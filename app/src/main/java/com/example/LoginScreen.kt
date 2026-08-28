package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Background Movie Poster Collage / Grid
        NetflixCollageBackground()

        // Top-left NETFLIX Logo / App Name
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
        ) {
            Text(
                text = "NETFLIX",
                color = Color(0xFFE50914),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }

        // Centered Login Card
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(450.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF000000).copy(alpha = 0.85f))
                .padding(48.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Iniciar sesión",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (errorMessage != null) {
                    Surface(
                        color = Color(0xFFE50914).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Username Field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text("Usuario", color = Color(0xFF8C8C8C)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF333333),
                        unfocusedContainerColor = Color(0xFF333333),
                        disabledContainerColor = Color(0xFF333333),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Contraseña", color = Color(0xFF8C8C8C)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF333333),
                        unfocusedContainerColor = Color(0xFF333333),
                        disabledContainerColor = Color(0xFF333333),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Login Button
                Button(
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            errorMessage = "Por favor ingresa usuario y contraseña"
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val success = withContext(Dispatchers.IO) {
                                    val client = OkHttpClient()
                                    val url = "${ServerConfig.SERVER_URL}/player_api.php?username=${username.trim()}&password=${password.trim()}"
                                    val request = Request.Builder().url(url).build()
                                    val response = client.newCall(request).execute()
                                    val body = response.body?.string()
                                    if (body != null) {
                                        val json = JSONObject(body)
                                        val userInfo = json.optJSONObject("user_info")
                                        val auth = userInfo?.optInt("auth", 0) ?: 0
                                        auth == 1
                                    } else {
                                        false
                                    }
                                }
                                isLoading = false
                                if (success) {
                                    UserSession.username = username.trim()
                                    UserSession.password = password.trim()
                                    onLoginSuccess()
                                } else {
                                    errorMessage = "Usuario o contraseña incorrectos"
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "Error de conexión o credenciales inválidas"
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Iniciar sesión",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Options Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Acuérdate de mí",
                        color = Color(0xFFB3B3B3),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "¿Necesitas ayuda?",
                        color = Color(0xFFB3B3B3),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // reCAPTCHA notice
                Text(
                    text = "Esta página está protegida por Google reCAPTCHA para asegurar que no eres un bot. Más información.",
                    color = Color(0xFF8C8C8C),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
