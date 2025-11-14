package com.shipthis.go.ui.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.zxing.integration.android.IntentIntegrator
import com.shipthis.go.QrScannerActivity

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
            onScanned(intentResult?.contents)
        } else {
            onScanned(null)
        }
    }

    return remember {
        {
            val integrator = IntentIntegrator(activity).apply {
                setCaptureActivity(QrScannerActivity::class.java) // ★ portrait override
                setOrientationLocked(true)
                setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                setPrompt("Scan your ShipThis Go QR code")
                setBeepEnabled(false)
            }

            launcher.launch(integrator.createScanIntent())
        }
    }
}
