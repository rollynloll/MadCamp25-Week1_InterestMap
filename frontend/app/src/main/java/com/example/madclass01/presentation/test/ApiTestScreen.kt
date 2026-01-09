package com.example.madclass01.presentation.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.data.repository.ApiResult

@Composable
fun ApiTestScreen(
    viewModel: ApiTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "🧪 Backend API Test",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Health Check 테스트
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("1️⃣ Health Check", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.testHealthCheck() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Health Check")
                }
                
                if (uiState.healthCheckResult != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.healthCheckResult!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.healthCheckResult!!.contains("Success")) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Create User 테스트
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("2️⃣ Create User", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.testCreateUser() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Create User")
                }
                
                if (uiState.createUserResult != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.createUserResult!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.createUserResult!!.contains("Success")) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Get User 테스트
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("3️⃣ Get User", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.testGetUser() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Get User")
                }
                
                if (uiState.getUserResult != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.getUserResult!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.getUserResult!!.contains("Success")) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 전체 결과
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 서버 설정 정보
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ℹ️ Server Info", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = """
                        📍 Base URL: ${uiState.baseUrl}
                        
                        💡 Tips:
                        - 에뮬레이터: http://10.0.2.2:8000
                        - 실제 기기: http://[PC_IP]:8000
                        - 백엔드 실행: uvicorn app.main:app --reload
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

data class ApiTestUiState(
    val isLoading: Boolean = false,
    val healthCheckResult: String? = null,
    val createUserResult: String? = null,
    val getUserResult: String? = null,
    val baseUrl: String = "http://10.249.68.62:8000"
)
