package com.example.proyecto_final.view

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_final.R
import com.example.proyecto_final.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, viewModel: LoginViewModel = viewModel()) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            onLoginSuccess()
        }
    }

    val webClientId = "340173266979-a8o252hibu37080v6h98pqg7m2ojucck.apps.googleusercontent.com"
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    viewModel.signInWithGoogle(token, onLoginSuccess)
                }
            } catch (e: ApiException) {
                Log.e("LoginScreen", "Google Sign-In falló: ${e.statusCode}")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (viewModel.isRegisterMode) stringResource(R.string.btn_register) else stringResource(R.string.login_welcome),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E)
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text(stringResource(R.string.label_email)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text(stringResource(R.string.label_password)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        if (viewModel.errorMessage != null) {
            Text(viewModel.errorMessage!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { 
                if (viewModel.isRegisterMode) viewModel.register(onLoginSuccess) 
                else viewModel.login(onLoginSuccess) 
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text(if (viewModel.isRegisterMode) stringResource(R.string.btn_register) else stringResource(R.string.btn_login))
        }

        TextButton(onClick = { viewModel.toggleMode() }) {
            Text(if (viewModel.isRegisterMode) stringResource(R.string.switch_to_login) else stringResource(R.string.switch_to_register))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("O", color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { 
                googleSignInClient.signOut().addOnCompleteListener {
                    launcher.launch(googleSignInClient.signInIntent)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.login_button_google), color = Color.Black)
        }
    }
}
