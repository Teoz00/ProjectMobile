package com.example.projectmobile.ui

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

// Funzione per creare il client Google Sign-In
fun getGoogleSignInClient(activity: Activity) = GoogleSignIn.getClient(
    activity,
    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("427093130970-8n3m8prn62jq4uukls9tb3odfu65cndh.apps.googleusercontent.com")
        .requestEmail()
        .build()
)

// Funzione per estrarre l'account Google dall'intent risultato
fun handleSignInResult(completedTask: Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount>) =
    try {
        completedTask.getResult(ApiException::class.java)
    } catch (e: ApiException) {
        null
    }