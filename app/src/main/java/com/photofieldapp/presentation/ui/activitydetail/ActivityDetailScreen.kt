package com.photofieldapp.presentation.ui.activitydetail

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.photofieldapp.R
import com.photofieldapp.data.local.entity.Photo
import com.photofieldapp.presentation.viewmodel.ActivityDetailViewModel
import com.photofieldapp.util.CameraUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ActivityDetailScreen : androidx.activity.ComponentActivity() {
    private val viewModel: ActivityDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            com.photofieldapp.ui.theme.Theme.PhotoFieldApp {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ActivityDetailContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ActivityDetailContent(
    viewModel: ActivityDetailViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var cameraSelector by remember { mutableStateOf(androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_AUTO) }
    var autoFocusEnabled by remember { mutableStateOf(true) }

    // Camera lifecycle
    androidx.lifecycle.LifecycleObserver(
        object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onStart(owner: androidx.lifecycle.LifecycleOwner) {
                if (previewView != null && imageCapture != null) {
                    viewModel.onCameraReady(owner, previewView!!, imageCapture!!)
                }
            }

            override fun onStop(owner: androidx.lifecycle.LifecycleOwner) {
                viewModel.onCameraReleased()
            }
        }
    )

    LaunchedEffect(Unit) {
        val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
        val cameraUtil = com.photofieldapp.util.CameraUtil.getInstance(context, cameraProviderFuture)

        // Initialize ImageCapture
        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .build()
        imageCapture = capture
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.activity_detail_title, "Activity")) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { /* navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setSelectedPhotoForMap(viewModel.getPhotosWithLocation().firstOrNull()) }) {
                        Icon(Icons.Default.Map, contentDescription = stringResource(R.string.view_map))
                    }
                    IconButton(onClick = { viewModel.generatePdf { success, path ->
                        if (success) {
                            // Show success snackbar
                        }
                    } }, enabled = !uiState.isGeneratingPdf) {
                        if (uiState.isGeneratingPdf) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = stringResource(R.string.export_pdf))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            // Camera Preview
            if (uiState.isCameraActive || true) {
                CameraPreviewSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f/3f)
                        .padding(16.dp),
                    previewView = previewView,
                    onPreviewViewCreated = { pv ->
                        previewView = pv
                        // Rebind if imageCapture is ready
                        if (imageCapture != null) {
                            val owner = androidx.lifecycle.LocalLifecycleOwner.current
                            viewModel.onCameraReady(owner, pv, imageCapture!!)
                        }
                    }
                )
            }

            // Camera Controls
            CameraControls(
                onCaptureClick = {
                    imageCapture?.let { capture ->
                        viewModel.takePhoto(capture) { success, error ->
                            // Handle result
                        }
                    }
                },
                onSwitchCamera = {
                    // Switch camera
                },
                onFlashToggle = {
                    flashMode = when (flashMode) {
                        ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                        ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                        else -> ImageCapture.FLASH_MODE_AUTO
                    }
                    imageCapture?.flashMode = flashMode
                },
                onAutoFocusToggle = {
                    autoFocusEnabled = !autoFocusEnabled
                },
                isCapturing = uiState.isCapturing,
                flashMode = flashMode,
                autoFocusEnabled = autoFocusEnabled
            )

            // Photo Grid
            PhotoGridSection(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                photos = uiState.photos,
                onPhotoClick = { photo ->
                    // Show full screen image
                },
                onPhotoLongClick = { photo ->
                    viewModel.setShowDeleteConfirm(photo.id)
                },
                onReorder = { from, to ->
                    viewModel.reorderPhotos(from, to)
                }
            )
        }
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteConfirm != -1L) {
        val photoToDelete = uiState.photos.find { it.id == uiState.showDeleteConfirm }
        photoToDelete?.let { photo ->
            AlertDialog(
                onDismissRequest = { viewModel.cancelDeletePhoto() },
                title = { Text(stringResource(R.string.delete_confirm)) },
                text = { Text("Hapus foto ini? Tindakan ini tidak dapat dibatalkan.") },
                confirmButton = {
                    FilledButton(
                        onClick = { viewModel.confirmDeletePhoto(photo.id) },
                        colors = ButtonDefaults.filledButtonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text(stringResource(R.string.btn_delete)) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDeletePhoto() }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }
    }

    // PDF Generation Progress
    uiState.pdfProgress?.let { progress ->
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.pdf_generating)) },
            text = {
                Column(modifier = Modifier.padding(16.dp)) {
                    LinearProgressIndicator(progress = progress.currentPage.toFloat() / progress.totalPages)
                    Text("Halaman ${progress.currentPage} dari ${progress.totalPages}")
                    Text("Foto ${progress.currentPhotoIndex} dari ${progress.totalPhotos}")
                }
            }
        )
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
fun CameraPreviewSection(
    modifier: Modifier = Modifier,
    previewView: PreviewView?,
    onPreviewViewCreated: (PreviewView) -> Unit
) {
    val context = LocalContext.current
    AndroidView(
        factory = { PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        } },
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        update = { pv ->
            if (pv !== previewView) {
                onPreviewViewCreated(pv)
            }
        }
    )
}

@Composable
fun CameraControls(
    onCaptureClick: () -> Unit,
    onSwitchCamera: () -> Unit,
    onFlashToggle: () -> Unit,
    onAutoFocusToggle: () -> Unit,
    isCapturing: Boolean,
    flashMode: Int,
    autoFocusEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Switch Camera
        IconButton(onClick = onSwitchCamera) {
            Icon(Icons.Default.CameraFront, contentDescription = stringResource(R.string.switch_camera))
        }

        // Flash Toggle
        IconButton(onClick = onFlashToggle) {
            val flashIcon = when (flashMode) {
                ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                ImageCapture.FLASH_MODE_OFF -> Icons.Default.FlashOff
                else -> Icons.Default.FlashAuto
            }
            Icon(flashIcon, contentDescription = stringResource(R.string.toggle_flash))
        }

        // Auto Focus Toggle
        IconButton(onClick = onAutoFocusToggle) {
            Icon(
                Icons.Default.Check,
                contentDescription = if (autoFocusEnabled) stringResource(R.string.autofocus_on) else stringResource(R.string.autofocus_off),
                tint = if (autoFocusEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Capture Button
        FloatingActionButton(
            onClick = onCaptureClick,
            enabled = !isCapturing,
            modifier = Modifier.size(72.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            if (isCapturing) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp))
            } else {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.capture_photo), modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun PhotoGridSection(
    modifier: Modifier = Modifier,
    photos: List<Photo>,
    onPhotoClick: (Photo) -> Unit,
    onPhotoLongClick: (Photo) -> Unit,
    onReorder: (Int, Int) -> Unit
) {
    if (photos.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Text(stringResource(R.string.activity_empty), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.activity_empty_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(photos) { photo ->
                PhotoItem(
                    photo = photo,
                    onClick = { onPhotoClick(photo) },
                    onLongClick = { onPhotoLongClick(photo) },
                    onDelete = { /* handled by long click */ }
                )
            }
        }
    }
}

@Composable
fun PhotoItem(
    photo: Photo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val imageFile = com.photofieldapp.util.FileStorageUtil.getAbsolutePath(context, photo.filePath)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp, 60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (imageFile.exists()) {
                    androidx.compose.ui.res.painterResource(imageFile.absolutePath) // This won't work directly - need Coil
                    // Use Coil's AsyncImage instead
                    com.coil.compose.AsyncImage(
                        model = imageFile,
                        contentDescription = stringResource(R.string.cd_photo_thumbnail),
                        contentScale = androidx.compose.foundation.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Icon(Icons.Default.BrokenImage, contentDescription = "Missing image", modifier = Modifier.size(32.dp).align(Alignment.Center), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = photo.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Foto ${photo.sortOrder + 1}  •  ${android.text.format.DateFormat.format("dd MMM HH:mm", photo.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (photo.hasLocation) {
                    Text(
                        text = "📍 ${String.format("%.6f", photo.latitude)}, ${String.format("%.6f", photo.longitude)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (photo.accuracy > 0) {
                    Text(
                        text = "Akurasi: ±${photo.accuracy.toInt()}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            photo.accuracy < 10 -> MaterialTheme.colorScheme.primary
                            photo.accuracy < 50 -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            // Drag handle / Menu
            IconButton(onClick = onLongClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
        }
    }
}