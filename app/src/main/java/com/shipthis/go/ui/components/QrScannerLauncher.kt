package com.shipthis.go.ui.components

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.zxing.integration.android.IntentIntegrator

@Composable
fun rememberQrScannerLauncher(
    activity: Activity,
    onScanned: (String?) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intentResult = IntentIntegrator.parseActivityResult(
                result.resultCode,
                result.data
            )
            val contents = intentResult?.contents
            onScanned(contents)
        } else {
            onScanned(null)
        }
    }

    return remember {
        {
            val integrator = IntentIntegrator(activity)
            integrator.setOrientationLocked(false)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setPrompt("Scan a QR code")
            integrator.setBeepEnabled(true)
            val intent = integrator.createScanIntent()
            launcher.launch(intent)
        }
    }
}
