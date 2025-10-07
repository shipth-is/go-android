package com.shipthis.go.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.shipthis.go.GodotApp

import com.shipthis.go.data.repository.SampleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SampleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Hardcoded game URL
    private val gameUrl =
        "https://buymelunch-develop.ams3.cdn.digitaloceanspaces.com/test/assets-demo-2025-09-05.zip"

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

                // Simulate brief delay
                delay(500)

                val assetsDir = File(context.filesDir, "assets")
                val zipFile = File(context.cacheDir, "game.zip")

                updateStatus("Cleaning old files…")
                deleteRecursively(assetsDir)
                assetsDir.mkdirs()

                updateStatus("Downloading game…")
                downloadFile(gameUrl, zipFile) { p ->
                    updateStatus("Downloading… $p%")
                }

                updateStatus("Unzipping game…")
                unzip(zipFile, assetsDir) { p ->
                    updateStatus("Unzipping… $p%")
                }

                updateStatus("Launching…")

                // Launch GodotApp activity
                    val intent = Intent(context, GodotApp::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        data = "Build ID '$buildId' submitted and game launched!",
                        error = null
                    )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed: unknown error"
                )
            }
        }
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
        if (code != HttpURLConnection.HTTP_OK) throw IOException("HTTP $code: ${conn.responseMessage}")

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
        ZipInputStream(FileInputStream(zip)).use { zis ->
            while (zis.nextEntry != null) count++
        }
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
