package com.photofieldapp.presentation.ui.map

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.photofieldapp.R
import com.photofieldapp.presentation.viewmodel.MapViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapScreen : androidx.activity.ComponentActivity() {
    private val viewModel: MapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            com.photofieldapp.ui.theme.Theme.PhotoFieldApp {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MapContent(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { /* navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.centerOnMarkers() }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Center map")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // osmdroid MapView
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                markers = uiState.photoMarkers,
                onMarkerClick = { marker ->
                    viewModel.selectMarker(marker)
                },
                cameraPosition = uiState.cameraPosition,
                onCameraChange = { position ->
                    viewModel.updateCameraPosition(position)
                }
            )

            // Selected marker info
            uiState.selectedMarker?.let { marker ->
                MarkerInfoBottomSheet(
                    marker = marker,
                    onDismiss = { viewModel.selectMarker(null) },
                    onNavigate = { /* navigate to activity detail */ }
                )
            }

            if (uiState.photoMarkers.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Text(stringResource(R.string.map_no_photos), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    uiState.error?.let { error ->
        SnackbarHost(modifier = Modifier) { data ->
            Snackbar(data, action = { /* dismiss */ }) {
                Text(text = error)
            }
        }
    }
}

@Composable
fun MarkerInfoBottomSheet(
    marker: com.photofieldapp.presentation.viewmodel.MapViewModel.PhotoMarker,
    onDismiss: () -> Unit,
    onNavigate: () -> Unit
) {
    // Bottom sheet implementation
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(marker.title, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Text(marker.snippet, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                FilledButton(onClick = onNavigate) {
                    Text("Lihat Activity")
                }
            }
        }
    }
}