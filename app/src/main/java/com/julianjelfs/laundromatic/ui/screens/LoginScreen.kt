package com.julianjelfs.laundromatic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.julianjelfs.laundromatic.AppUiState

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    uiState: AppUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.LocalLaundryService,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = "Laundromatic",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                text = "Sign in with existing Firebase email and password.",
                style = MaterialTheme.typography.bodyLarge,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !uiState.isAuthenticating,
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        enabled = !uiState.isAuthenticating,
                    )

                    uiState.authError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isAuthenticating &&
                            uiState.email.isNotBlank() &&
                            uiState.password.isNotBlank(),
                        onClick = onSignIn,
                    ) {
                        Text(if (uiState.isAuthenticating) "Signing in..." else "Sign in")
                    }
                }
            }
        }
    }
}
