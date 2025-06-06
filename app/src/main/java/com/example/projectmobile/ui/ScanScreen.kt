package com.example.projectmobile.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

@Composable
fun ScanScreen(
    onAddScannedItem: (FoodItem) -> Unit
) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    var expirationDate by remember { mutableStateOf<String?>(null) }
    var productName by remember { mutableStateOf<String?>(null) }
    var productImageUri by remember { mutableStateOf<Uri?>(null) }
    var isBarcodeMode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val productCache = remember { ConcurrentHashMap<String, Triple<String, String?, Uri?>>() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(hasCameraPermission, previewView, isBarcodeMode) {
        if (hasCameraPermission && previewView != null) {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView!!.surfaceProvider)
            }
            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        Executors.newSingleThreadExecutor(),
                        if (isBarcodeMode) {
                            BarcodeAnalyzer { barcode ->
                                if (productCache.containsKey(barcode)) {
                                    val (cachedName, cachedExpiry, cachedUri) =
                                        productCache[barcode]!!
                                    productName = cachedName
                                    expirationDate = cachedExpiry
                                    productImageUri = cachedUri
                                    errorMessage = null
                                } else {
                                    fetchProductInfo(barcode,
                                        { name, expiry, imgUrl ->
                                            val uriParsed = imgUrl?.let { Uri.parse(it) }
                                            productName = name
                                            expirationDate = expiry
                                            productImageUri = uriParsed
                                            errorMessage = null
                                            productCache[barcode] = Triple(name, expiry, uriParsed)
                                        },
                                        { err ->
                                            errorMessage = err
                                        }
                                    )
                                }
                            }
                        } else {
                            ExpirationDateAnalyzer { date ->
                                expirationDate = date
                                errorMessage = null
                            }
                        }
                    )
                }
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analyzer
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasCameraPermission) {
            Box(modifier = Modifier.weight(1f)) {
                AndroidView(
                    factory = { ctx -> PreviewView(ctx).also { previewView = it } },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Mostriamo le info recuperate
            expirationDate?.let {
                Text("Expiration Date: $it", modifier = Modifier.padding(8.dp))
            }
            productName?.let {
                Text("Product Name: $it", modifier = Modifier.padding(8.dp))
            }
            productImageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            errorMessage?.let {
                Text("Error: $it", modifier = Modifier.padding(8.dp), color = Color.Red)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PULSANTE “Add to Fridge”
            if (productName != null && expirationDate != null) {
                Button(
                    onClick = {
                        val newItem = FoodItem(
                            name = productName!!,
                            expirationDate = expirationDate!!,
                            imageUri = productImageUri
                        )
                        onAddScannedItem(newItem)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6D8E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = "Add to Fridge",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { isBarcodeMode = !isBarcodeMode }) {
                Text(if (isBarcodeMode) "Switch to Text Scan" else "Switch to Barcode Scan")
            }
        } else {
            Text("Camera permission is required")
        }
    }
}

fun fetchProductInfo(
    barcode: String,
    onResult: (String, String?, String?) -> Unit,
    onError: (String) -> Unit
) {
    val url = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
    val request = Request.Builder().url(url).build()
    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("API", "Request failed", e)
            onError("Connection failed")
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { bodyString ->
                try {
                    val json = JSONObject(bodyString)
                    if (json.optInt("status") == 1) {
                        val product = json.optJSONObject("product")
                        val name = product?.optString("product_name", "Unknown") ?: "Unknown"
                        val expiry = product?.optString("expiration_date", null)
                        val imgUrl = product?.optString(
                            "image_front_small_url",
                            product.optString("image_url", null)
                        )
                        onResult(name, expiry, imgUrl)
                    } else {
                        onError("Product not found")
                    }
                } catch (e: Exception) {
                    Log.e("API", "JSON parse error", e)
                    onError("Parsing error")
                }
            } ?: onError("Empty response")
        }
    })
}

fun extractDateFromText(text: String): String? {
    val dateRegex = Regex("""\b([0-3]?\d[\/\.\- ][0-1]?\d[\/\.\- ][0-9]{2,4})\b""")
    return dateRegex.find(text)?.groupValues?.get(1)
}

class ExpirationDateAnalyzer(private val onDateDetected: (String) -> Unit) : ImageAnalysis.Analyzer {
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val input = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(input)
                .addOnSuccessListener { visionText ->
                    extractDateFromText(visionText.text)?.let(onDateDetected)
                }
                .addOnFailureListener { e -> Log.e("Analyzer", "Text recognition failed", e) }
                .addOnCompleteListener { image.close() }
        } else image.close()
    }
}

class BarcodeAnalyzer(private val onBarcodeScanned: (String) -> Unit) : ImageAnalysis.Analyzer {
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let {
                            onBarcodeScanned(it)
                            // break  // se voglio fermarmi al primo
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BarcodeAnalyzer", "Scanning failed", e)
                }
                .addOnCompleteListener { imageProxy.close() }
        } else imageProxy.close()
    }
}
