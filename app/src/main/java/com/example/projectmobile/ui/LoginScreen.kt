package com.example.projectmobile.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import com.example.projectmobile.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel()) {
    val authState by loginViewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context as Activity

    // Google Sign-In launcher
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val account = handleSignInResult(task)
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        navController.navigate("fridge") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        Toast.makeText(activity, "Google login failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(activity, "Google sign in error", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthResult.Success) {
            navController.navigate("fridge") {
                popUpTo("login") { inclusive = true }
            }
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
        Text("LOGIN", style = MaterialTheme.typography.h5,color = Color(0xFF3E6D8E))

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
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
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                    tint = Color(0xFF3E6D8E)
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
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
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password Icon",
                    tint = Color(0xFF3E6D8E)
                )
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { loginViewModel.login(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF3E6D8E),
                contentColor = Color.White
            )
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -- Pulsante Login con Google --
        Button(
            onClick = {
                val signInClient = getGoogleSignInClient(activity)  // funzione dal tuo AuthResult
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

        when (authState) {
            is AuthResult.Loading -> CircularProgressIndicator()
            is AuthResult.Error -> Text(
                text = (authState as AuthResult.Error).message,
                color = MaterialTheme.colors.error
            )
            else -> {}
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            navController.navigate("register")
        }) {
            Text(
                "Don’t have an account? Sign up",
                color = Color(0xFF3E6D8E)
            )
        }
    }
}
