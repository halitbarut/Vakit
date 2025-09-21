package com.halitbarut.vakit

import android.os.Bundle
import com.halitbarut.vakit.navigation.VakitNavGraph
import com.halitbarut.vakit.ui.theme.VakitTheme
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.halitbarut.vakit.ui.MainUiState
import com.halitbarut.vakit.ui.MainViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState = mainViewModel.uiState.collectAsStateWithLifecycle().value
            VakitRoot(uiState = uiState)
        }
    }
}

@Composable
fun VakitRoot(uiState: MainUiState) {
    VakitTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val navController = rememberNavController()
                VakitNavGraph(
                    navController = navController,
                    startDestination = uiState.startDestination,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VakitPreview() {
    VakitRoot(uiState = MainUiState(isLoading = false))
}
