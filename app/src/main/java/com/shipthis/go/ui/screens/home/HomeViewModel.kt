package com.shipthis.go.ui.screens.home

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.shipthis.go.data.repository.GoBuildRepository
import com.shipthis.go.util.CrashMarker
import com.shipthis.go.util.SocketLogHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GoBuildRepository,
    private val socketLogHandler: SocketLogHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun updateBuildId(buildId: String) {
        _uiState.value = _uiState.value.copy(buildId = buildId)
    }

    fun submitBuildId(context: Context) {
        val buildId = _uiState.value.buildId
        if (buildId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Build ID cannot be empty")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null, data = null)

                updateStatus("Fetching build details…")

                // Fetch the GoBuild from the API
                val goBuild = repository.getGoBuild(buildId)

                updateStatus("Build found: ${goBuild.project.name}")

                val assetsDir = File(context.filesDir, "assets")
                val zipFile = File(context.cacheDir, "game.zip")

                updateStatus("Cleaning old files…")
                deleteRecursively(assetsDir)
                assetsDir.mkdirs()

                updateStatus("Downloading game…")

                downloadFile(goBuild.url, zipFile) { p -> updateStatus("Downloading… $p%") }

                updateStatus("Unzipping game…")
                unzip(zipFile, assetsDir) { p -> updateStatus("Unzipping… $p%") }

                updateStatus("Launching…")

                // Set buildId in log handler for runtime logging
                socketLogHandler.setBuildId(goBuild.id)

                // Mark the start of this runtime session
                CrashMarker.markStart(context, goBuild.id)

                var gameEngineVersion = goBuild.jobDetails.gameEngineVersion

                launchGame(context, gameEngineVersion)

                _uiState.value =
                        _uiState.value.copy(
                                isLoading = false,
                                data =
                                        "Build '${goBuild.project.name}' (${goBuild.id}) launched successfully!",
                                error = null
                        )
            } catch (e: Exception) {
                _uiState.value =
                        _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed: unknown error"
                        )
            }
        }
    }

    private fun launchGame(context: Context, version: String) {
        val manager = SplitInstallManagerFactory.create(context)
        val moduleName =
                when (version) {
                    "4.5", "4.5.1" -> "godot_v4_5"
                    "3.6", "3.6.1", "3.6.2" -> "godot_v3_x"
                    else -> "godot_v4_5"
                }

        if (manager.installedModules.contains(moduleName)) {
            startGodotActivity(context, version)
            return
        }

        val request = SplitInstallRequest.newBuilder().addModule(moduleName).build()

        val listener = object : SplitInstallStateUpdatedListener {
            override fun onStateUpdate(state: SplitInstallSessionState) {
                if (state.moduleNames().contains(moduleName)) {
                    when (state.status()) {
                        SplitInstallSessionStatus.DOWNLOADING -> {
                            Log.i(
                                    "ShipThis",
                                    "Downloading $moduleName (${state.bytesDownloaded()}/${state.totalBytesToDownload()})"
                            )
                        }
                        SplitInstallSessionStatus.INSTALLING -> {
                            Log.i("ShipThis", "Installing $moduleName…")
                        }
                        SplitInstallSessionStatus.INSTALLED -> {
                            Log.i("ShipThis", "$moduleName installed, launching")
                            manager.unregisterListener(this)
                            startGodotActivity(context, version)
                        }
                        SplitInstallSessionStatus.FAILED -> {
                            Log.e("ShipThis", "Install failed: ${state.errorCode()}")
                            manager.unregisterListener(this)
                        }
                    }
                }
            }
        }

        manager.registerListener(listener)
        manager.startInstall(request)
    }

    private fun startGodotActivity(context: Context, version: String) {
        val className =
                when (version) {
                    "4.5", "4.5.1" -> "com.shipthis.go.GodotAppv4_5"
                    "3.6", "3.6.1", "3.6.2" -> "com.shipthis.go.GodotAppv3_x"
                    else -> "com.shipthis.go.GodotAppv4_5"
                }

        val intent = Intent()
        intent.setClassName(context.packageName, className)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun updateStatus(msg: String) {
        _uiState.value = _uiState.value.copy(data = msg)
    }

    // ---- IO helpers ----

    private fun downloadFile(urlString: String, out: File, onProgress: (Int) -> Unit) {
        val conn = URL(urlString).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 300000
        conn.connect()
        val code = conn.responseCode
        if (code != HttpURLConnection.HTTP_OK)
                throw IOException("HTTP $code: ${conn.responseMessage}")

        val length = conn.contentLength
        var total = 0L
        var lastPct = -1
        conn.inputStream.buffered().use { input ->
            FileOutputStream(out).buffered().use { output ->
                val buf = ByteArray(8192)
                while (true) {
                    val n = input.read(buf)
                    if (n == -1) break
                    output.write(buf, 0, n)
                    total += n
                    if (length > 0) {
                        val pct = (total * 100 / length).toInt()
                        if (pct != lastPct) {
                            lastPct = pct
                            onProgress(pct)
                        }
                    }
                }
            }
        }
        conn.disconnect()
    }

    private fun unzip(zipFile: File, targetDir: File, onProgress: (Int) -> Unit) {
        val totalEntries = countZipEntries(zipFile).coerceAtLeast(1)
        var done = 0
        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry: ZipEntry?
            val buf = ByteArray(8192)
            while (true) {
                entry = zis.nextEntry ?: break
                val outFile = File(targetDir, entry.name)

                // Zip Slip guard – prevents writing outside targetDir
                val canonicalPath = outFile.canonicalPath
                val canonicalTarget = targetDir.canonicalPath + File.separator
                if (!canonicalPath.startsWith(canonicalTarget)) {
                    throw IOException("Blocked Zip Slip: ${entry.name}")
                }

                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { os ->
                        while (true) {
                            val n = zis.read(buf)
                            if (n == -1) break
                            os.write(buf, 0, n)
                        }
                    }
                }
                zis.closeEntry()
                done++
                onProgress((done * 100) / totalEntries)
            }
        }
    }

    private fun countZipEntries(zip: File): Int {
        var count = 0
        ZipInputStream(FileInputStream(zip)).use { zis -> while (zis.nextEntry != null) count++ }
        return count
    }

    private fun deleteRecursively(f: File) {
        if (!f.exists()) return
        if (f.isDirectory) f.listFiles()?.forEach { deleteRecursively(it) }
        f.delete()
    }
}

data class HomeUiState(
        val isLoading: Boolean = false,
        val data: String? = null,
        val error: String? = null,
        val buildId: String = ""
)
