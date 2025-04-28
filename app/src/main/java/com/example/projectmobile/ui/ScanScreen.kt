package com.example.projectmobile.ui

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun ScanScreen() {
    var hasCameraPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageAnalyzer by remember { mutableStateOf<ImageAnalysis?>(null) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var expirationDate by remember { mutableStateOf<String?>(null) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
        }
    )

    LaunchedEffect(key1 = true) {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(key1 = hasCameraPermission) {
        if (hasCameraPermission) {
            cameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder().build()
            imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(
                    Executors.newSingleThreadExecutor(),
                    ExpirationDateAnalyzer { date ->
                        expirationDate = date
                    }
                )
            }
            imageCapture = ImageCapture.Builder().build()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasCameraPermission) {
            Box(modifier = Modifier.weight(1f)) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        preview?.setSurfaceProvider(previewView.surfaceProvider)
                        cameraProvider?.unbindAll()
                        cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer,
                            imageCapture
                        )
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Button(onClick = {
                val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault())
                    .format(System.currentTimeMillis())
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
                    }
                }

                val outputOptions = ImageCapture.OutputFileOptions
                    .Builder(
                        context.contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )
                    .build()

                imageCapture?.takePicture(
                    outputOptions,
                    Executors.newSingleThreadExecutor(),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            Log.d("ScanScreen", "Image saved in ${outputFileResults.savedUri}")
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("ScanScreen", "Error saving image: ${exception.message}", exception)
                        }
                    }
                )
            }) {
                Text("Take Picture")
            }
            if (expirationDate != null) {
                Text(text = "Expiration Date: $expirationDate")
            }
        } else {
            Text("Camera permission is required")
        }
    }
}

fun extractDateFromText(text: String): String {
    // Placeholder logic to simulate extraction
    val regex = "\\d{2}/\\d{2}/\\d{4}".toRegex() // Per esempio: 12/31/2024
    return regex.find(text)?.value ?: "No date found"
}


class ExpirationDateAnalyzer(private val onDateDetected: (String) -> Unit) :
    androidx.camera.core.ImageAnalysis.Analyzer {
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: androidx.camera.core.ImageProxy) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val inputImage = com.google.mlkit.vision.common.InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
            val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    // Process the recognized text
                    val date = extractDateFromText(visionText.text)
                    onDateDetected(date)
                }
                .addOnFailureListener { e ->
                    Log.e("ExpirationDateAnalyzer", "Error analyzing image", e)
                }
                .addOnCompleteListener {
                    image.close()
                }
        }
    }
}