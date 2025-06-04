package com.example.projectmobile.ui

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.projectmobile.R
import androidx.compose.ui.graphics.painter.Painter
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import android.graphics.*
import android.util.Base64
import java.io.ByteArrayOutputStream
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val activity = context as Activity
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance().reference

    var username by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }
    var birthDateError by remember { mutableStateOf(false) }

    // Image picker
    val imageUriState = remember { mutableStateOf<Uri?>(null) }
    val imageUri = imageUriState.value
    val launcherImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUriState.value = uri }

    var profileImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var profileImageBase64 by remember { mutableStateOf("") }
    var assignedColor by remember { mutableStateOf<Color?>(null) }

    // Aggiorna immagine profilo quando cambia username
    LaunchedEffect(username) {
        if (username.isNotBlank()) {
            // Se è la prima lettera e non c'è ancora assignedColor -> assegnalo
            if (assignedColor == null) {
                assignedColor = generateRandomColor()
            }
            val color = assignedColor ?: generateRandomColor()
            val initial = username[0]
            val bitmap = generateProfileImage(initial, color)
            profileImageBitmap = bitmap
            profileImageBase64 = bitmapToBase64(bitmap)
        } else {
            profileImageBitmap = null
            profileImageBase64 = ""
            assignedColor = null // resetta colore se username viene svuotato
        }
    }

    // Google Sign-In launcher
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val account = handleSignInResult(task)
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        Toast.makeText(context, "Google account registered", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    } else {
                        Toast.makeText(context, "Google registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(context, "Google sign in error", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F1F6))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("REGISTER", style = MaterialTheme.typography.h5,color = Color(0xFF3E6D8E))

        Spacer(modifier = Modifier.height(16.dp))
        // Foto profilo circolare con "+" per scegliere
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(70))
                .clickable { launcherImagePicker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            when {
                imageUriState.value != null -> {
                    // Se vuoi disabilitare caricamento immagine utente, puoi rimuovere questo blocco
                }
                profileImageBitmap != null -> {
                    Image(
                        bitmap = profileImageBitmap!!.asImageBitmap(),
                        contentDescription = "Generated Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(70))
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Icon",
                        tint = Color.White,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = username,
            onValueChange = {
                username = it
                usernameError = false },
            label = { Text("Username") },
            singleLine = true,
            isError = usernameError,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White,
                focusedIndicatorColor = Color(0xFF3E6D8E),
                unfocusedIndicatorColor = Color(0xFF3E6D8E),
                cursorColor = Color(0xFF3E6D8E),
                focusedLabelColor = Color(0xFF3E6D8E)
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF3E6D8E)
                )
            }
        )
        if (usernameError) Text("Username required", color = Color.Red)

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = birthDate,
            onValueChange = {
                birthDate = it
                birthDateError = false },
            label = { Text("Birth Date (dd/mm/yyyy)") },
            singleLine = true,
            isError = birthDateError,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White,
                focusedIndicatorColor = Color(0xFF3E6D8E),
                unfocusedIndicatorColor = Color(0xFF3E6D8E),
                cursorColor = Color(0xFF3E6D8E),
                focusedLabelColor = Color(0xFF3E6D8E)
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color(0xFF3E6D8E)
                )
            }
        )
        if (birthDateError) Text("Date of Birth required", color = Color.Red)

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = {
                email = it
                emailError = false},
            label = { Text("Email") },
            singleLine = true,
            isError = emailError,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White,
                focusedIndicatorColor = Color(0xFF3E6D8E),
                unfocusedIndicatorColor = Color(0xFF3E6D8E),
                cursorColor = Color(0xFF3E6D8E),
                focusedLabelColor = Color(0xFF3E6D8E)
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = Color(0xFF3E6D8E)
                )
            }
        )
        if (emailError) Text("Email required", color = Color.Red)
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false},
            label = { Text("Password") },
            singleLine = true,
            isError = passwordError,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White,
                focusedIndicatorColor = Color(0xFF3E6D8E),
                unfocusedIndicatorColor = Color(0xFF3E6D8E),
                cursorColor = Color(0xFF3E6D8E),
                focusedLabelColor = Color(0xFF3E6D8E)
            ),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF3E6D8E)
                )
            }
        )
        if (passwordError) Text("Password required", color = Color.Red)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                emailError = email.isBlank()
                passwordError = password.isBlank()
                usernameError = username.isBlank()
                birthDateError = birthDate.isBlank()

                if (emailError || passwordError || usernameError || birthDateError) {
                    Toast.makeText(context, "Fill in all the required fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                loading = true

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val userId = authResult.user?.uid ?: return@addOnSuccessListener

                        val saveUserToFirestore: (String) -> Unit = { imageUrl ->
                            val userData = hashMapOf(
                                "email" to email,
                                "username" to username,
                                "birthDate" to birthDate,
                                "profileImageBase64" to profileImageBase64
                            )

                            db.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener {
                                    loading = false
                                    Toast.makeText(context, "Registration completed", Toast.LENGTH_SHORT).show()
                                    navController.navigate("fridge") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener {
                                    loading = false
                                    Toast.makeText(context, "User save error: ${it.message}", Toast.LENGTH_LONG).show()
                                    auth.currentUser?.delete()
                                }
                        }

                        val profileImageToSave = if (profileImageBase64.isNotEmpty()) profileImageBase64 else ""

                        saveUserToFirestore(profileImageToSave)

                        /* Se immagine selezionata, la carichiamo prima
                        if (imageUri != null) {
                            val imageRef = storage.child("profile_images/$userId.jpg")
                            imageRef.putFile(imageUri)
                                .continueWithTask { task ->
                                    if (!task.isSuccessful) {
                                        task.exception?.let { throw it }
                                    }
                                    imageRef.downloadUrl
                                }
                                .addOnSuccessListener { downloadUrl ->
                                    saveUserToFirestore(downloadUrl.toString())
                                }
                                .addOnFailureListener {
                                    loading = false
                                    Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show()
                                    auth.currentUser?.delete()
                                }
                        } else {
                            // Nessuna immagine: salviamo senza URL immagine
                            saveUserToFirestore("")
                        }*/
                    }
                    .addOnFailureListener {
                        loading = false
                        Toast.makeText(context, "User creation error: ${it.message}", Toast.LENGTH_LONG).show()
                        if (it.message?.contains("email") == true) emailError = true
                    }
            },
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF3E6D8E),
                contentColor = Color.White
            )
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Register")
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        // pulsante per Google Sign-In --
        Button(
            onClick = {
                val signInClient = getGoogleSignInClient(activity)
                val signInIntent = signInClient.signInIntent
                launcher.launch(signInIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFFFFFFF),
                contentColor = Color(0xFF3E6D8E)
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign in with Google")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Login",color = Color(0xFF3E6D8E))
        }
    }
}

// Funzione per salvare immagine e dati utente su Firestore
/*
fun uploadProfileImageAndSaveUserData(
    context: Context,
    imageUri: Uri?,
    email: String,
    username: String,
    birthDate: String,
    onComplete: () -> Unit,
    onError: (String) -> Unit
) {
    if (imageUri == null) {
        onError("Profile image not selected")
        return
    }

    val storageRef = FirebaseStorage.getInstance().reference
    val fileRef = storageRef.child("profile_images/${UUID.randomUUID()}.jpg")

    fileRef.putFile(imageUri)
        .addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                val userData = mapOf(
                    "email" to email,
                    "username" to username,
                    "birthDate" to birthDate,
                    "profileImageUrl" to uri.toString()
                )
                FirebaseFirestore.getInstance().collection("users")
                    .document(email)
                    .set(userData)
                    .addOnSuccessListener { onComplete() }
                    .addOnFailureListener { e -> onError(e.message ?: "Firestore error") }
            }
        }
        .addOnFailureListener { e -> onError(e.message ?: "Upload error") }
}

private fun saveUser(
    userId: String,
    email: String,
    username: String,
    birthDate: String,
    profileImageUrl: String,
    db: FirebaseFirestore,
    navController: NavController,
    context: Context,
    onComplete: () -> Unit
) {
    val userData = hashMapOf(
        "email" to email,
        "username" to username,
        "birthDate" to birthDate,
        "profileImageUrl" to profileImageUrl
    )

    db.collection("users").document(userId)
        .set(userData)
        .addOnSuccessListener {
            Toast.makeText(context, "Registration completed", Toast.LENGTH_SHORT).show()

            navController.navigate("fridge") {
                popUpTo("register") { inclusive = true }
            }

            onComplete()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Save error: ${it.message}", Toast.LENGTH_LONG).show()
            FirebaseAuth.getInstance().currentUser?.delete()
            onComplete()
        }
}
*/
fun generateRandomColor(): Color {
    val r = (0..255).random()
    val g = (0..255).random()
    val b = (0..255).random()
    return Color(r, g, b)
}

fun generateProfileImage(initial: Char, backgroundColor: Color, size: Int = 140): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    // Sfondo
    paint.color = android.graphics.Color.argb(255,
        (backgroundColor.red * 255).toInt(),
        (backgroundColor.green * 255).toInt(),
        (backgroundColor.blue * 255).toInt())
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

    // Testo
    paint.color = android.graphics.Color.WHITE
    paint.textSize = size * 0.6f
    paint.textAlign = Paint.Align.CENTER
    paint.typeface = Typeface.DEFAULT_BOLD
    paint.isAntiAlias = true

    val xPos = size / 2f
    val yPos = size / 2f - (paint.descent() + paint.ascent()) / 2

    canvas.drawText(initial.toString().uppercase(), xPos, yPos, paint)

    return bitmap
}

fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

