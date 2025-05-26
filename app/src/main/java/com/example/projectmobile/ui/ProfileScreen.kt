package com.example.projectmobile.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var username by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    var loading by remember { mutableStateOf(true) }

    // Per modifica password
    var isEditingPassword by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var savingPassword by remember { mutableStateOf(false) }

    // Dialog conferma eliminazione
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Launcher per picker immagine
    val launcherImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Qui puoi aggiungere la logica per upload su Firebase Storage e aggiornare Firestore
            profileImageUrl = uri.toString()
            Toast.makeText(context, "Profile picture updated locally", Toast.LENGTH_SHORT).show()
        }
    }

    // Carica dati utente da Firestore
    LaunchedEffect(user?.uid) {
        if (user == null) {
            navController.navigate("login") {
                popUpTo("profile") { inclusive = true }
            }
            return@LaunchedEffect
        }
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                username = doc.getString("username") ?: ""
                birthDate = doc.getString("birthDate") ?: ""
                profileImageUrl = doc.getString("profileImageUrl")
                loading = false
            }
            .addOnFailureListener {
                Toast.makeText(context, "Data loading error: ${it.message}", Toast.LENGTH_LONG).show()
                loading = false
            }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Dialog conferma eliminazione account
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm deletion") },
            text = { Text("Are you sure you want to delete your account? This action is irreversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        user?.uid?.let { uid ->
                            loading = true
                            db.collection("users").document(uid).delete()
                                .addOnSuccessListener {
                                    user.delete()
                                        .addOnSuccessListener {
                                            loading = false
                                            Toast.makeText(context, "Account successfully deleted", Toast.LENGTH_SHORT).show()
                                            navController.navigate("login") {
                                                popUpTo("profile") { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            loading = false
                                            Toast.makeText(context, "Account deletion error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    loading = false
                                    Toast.makeText(context, "Error deleting user data: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F1F6))
    ) {
        // Header piena larghezza blu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(Color(0xFF3E6D8E)),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Profile",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Foto profilo cliccabile
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(70))
                    .clickable { launcherImagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUrl != null && profileImageUrl!!.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUrl),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(70))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Icon",
                        tint = Color.White,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Username TextField (readonly)
            TextField(
                value = username,
                onValueChange = { },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Username Icon",
                        tint = Color(0xFF3E6D8E)
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color(0xFF3E6D8E),
                    unfocusedIndicatorColor = Color(0xFF3E6D8E),
                    cursorColor = Color(0xFF3E6D8E),
                    focusedLabelColor = Color(0xFF3E6D8E),
                    disabledTextColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email TextField (readonly)
            TextField(
                value = email,
                onValueChange = { },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon",
                        tint = Color(0xFF3E6D8E)
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color(0xFF3E6D8E),
                    unfocusedIndicatorColor = Color(0xFF3E6D8E),
                    cursorColor = Color(0xFF3E6D8E),
                    focusedLabelColor = Color(0xFF3E6D8E),
                    disabledTextColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // BirthDate TextField (readonly)
            TextField(
                value = birthDate,
                onValueChange = { },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Birth Date") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Birth Date Icon",
                        tint = Color(0xFF3E6D8E)
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color(0xFF3E6D8E),
                    unfocusedIndicatorColor = Color(0xFF3E6D8E),
                    cursorColor = Color(0xFF3E6D8E),
                    focusedLabelColor = Color(0xFF3E6D8E),
                    disabledTextColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Password edit row
            if (isEditingPassword) {
                TextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        passwordError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("New Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon",
                            tint = Color(0xFF3E6D8E)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (newPassword.length < 6) {
                                passwordError = true
                                Toast.makeText(context, "Password must have at least 6 characters", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }
                            savingPassword = true
                            user?.updatePassword(newPassword)
                                ?.addOnCompleteListener { task ->
                                    savingPassword = false
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                        isEditingPassword = false
                                        newPassword = ""
                                    } else {
                                        Toast.makeText(context, "Password update error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }) {
                            if (savingPassword) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF3E6D8E)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save Password",
                                    tint = Color(0xFF3E6D8E)
                                )
                            }
                        }
                    },
                    isError = passwordError,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color(0xFF3E6D8E),
                        unfocusedIndicatorColor = Color(0xFF3E6D8E),
                        cursorColor = Color(0xFF3E6D8E),
                        focusedLabelColor = Color(0xFF3E6D8E),
                        errorIndicatorColor = Color.Red
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                // Qui la modifica: password nascosta come TextField readOnly identica agli altri campi
                TextField(
                    value = "********",
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon",
                            tint = Color(0xFF3E6D8E)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isEditingPassword = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Password",
                                tint = Color(0xFF3E6D8E)
                            )
                        }
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color(0xFF3E6D8E),
                        unfocusedIndicatorColor = Color(0xFF3E6D8E),
                        cursorColor = Color(0xFF3E6D8E),
                        focusedLabelColor = Color(0xFF3E6D8E),
                        disabledTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(100.dp))

            // Pulsante elimina account
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF8B0000),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Delete Account")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pulsante logout
            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF3E6D8E),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
