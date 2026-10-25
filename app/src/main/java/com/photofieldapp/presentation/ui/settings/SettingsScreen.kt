package com.photofieldapp.presentation.ui.settings

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.photofieldapp.R
import com.photofieldapp.presentation.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers

@AndroidEntryPoint
class SettingsScreen : androidx.activity.ComponentActivity() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            com.photofieldapp.ui.theme.Theme.PhotoFieldApp {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SettingsContent(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val cameraAutofocus by viewModel.cameraAutofocus
        .observeAsState(initial = true)
    val cameraFlashMode by viewModel.cameraFlashMode
        .observeAsState(initial = 0)
    val pdfOrientation by viewModel.pdfOrientation
        .observeAsState(initial = 0)
    val pdfPhotosPerPage by viewModel.pdfPhotosPerPage
        .observeAsState(initial = 2)
    val pdfIncludeGps by viewModel.pdfIncludeGps
        .observeAsState(initial = true)
    val pdfIncludeTimestamp by viewModel.pdfIncludeTimestamp
        .observeAsState(initial = true)
    val autoBackup by viewModel.autoBackup
        .observeAsState(initial = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { /* navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        ListView(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Camera Section
            SettingsSection(
                title = stringResource(R.string.settings_camera),
                icon = Icons.Default.Camera
            ) {
                SettingRow(
                    title = stringResource(R.string.pref_camera_autofocus),
                    subtitle = stringResource(R.string.pref_camera_autofocus_summary),
                    trailing = {
                        Switch(
                            checked = cameraAutofocus,
                            onCheckedChange = { viewModel.setCameraAutofocus(it) }
                        )
                    }
                )
                Divider(modifier = Modifier.padding(start = 72.dp))
                SettingRow(
                    title = stringResource(R.string.pref_camera_flash_default),
                    subtitle = "Mode flash default saat membuka kamera",
                    trailing = {
                        FlashModeSelector(
                            selectedMode = cameraFlashMode,
                            onModeChange = { viewModel.setCameraFlashMode(it) }
                        )
                    }
                )
            }

            // PDF Section
            SettingsSection(
                title = stringResource(R.string.settings_pdf),
                icon = Icons.Default.PictureAsPdf
            ) {
                SettingRow(
                    title = stringResource(R.string.pref_pdf_orientation),
                    subtitle = "Orientasi halaman default untuk ekspor PDF",
                    trailing = {
                        OrientationSelector(
                            selectedOrientation = pdfOrientation,
                            onOrientationChange = { viewModel.setPdfOrientation(it) }
                        )
                    }
                )
                Divider(modifier = Modifier.padding(start = 72.dp))
                SettingRow(
                    title = stringResource(R.string.pref_pdf_photos_per_page),
                    subtitle = "Jumlah foto per halaman PDF",
                    trailing = {
                        PhotosPerPageSelector(
                            selectedCount = pdfPhotosPerPage,
                            onCountChange = { viewModel.setPdfPhotosPerPage(it) }
                        )
                    }
                )
                Divider(modifier = Modifier.padding(start = 72.dp))
                SettingRow(
                    title = stringResource(R.string.pref_pdf_include_gps),
                    subtitle = "Tampilkan koordinat GPS di bawah foto",
                    trailing = {
                        Switch(
                            checked = pdfIncludeGps,
                            onCheckedChange = { viewModel.setPdfIncludeGps(it) }
                        )
                    }
                )
                Divider(modifier = Modifier.padding(start = 72.dp))
                SettingRow(
                    title = stringResource(R.string.pref_pdf_include_timestamp),
                    subtitle = "Tampilkan waktu pengambilan foto",
                    trailing = {
                        Switch(
                            checked = pdfIncludeTimestamp,
                            onCheckedChange = { viewModel.setPdfIncludeTimestamp(it) }
                        )
                    }
                )
            }

            // Storage Section
            SettingsSection(
                title = stringResource(R.string.settings_storage),
                icon = Icons.Default.Storage
            ) {
                SettingRow(
                    title = stringResource(R.string.pref_storage_location),
                    subtitle = "Lokasi penyimpanan foto dan PDF",
                    trailing = {
                        Text("Internal/App", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                )
                Divider(modifier = Modifier.padding(start = 72.dp))
                SettingRow(
                    title = "Ukuran Penyimpanan",
                    subtitle = viewModel.getStorageInfo(),
                    trailing = {
                        TextButton(onClick = { /* open file manager */ }) {
                            Text("Kelola")
                        }
                    }
                )
                Divider(modifier = Modifier.padding(start = 72.dp))
                SettingRow(
                    title = stringResource(R.string.pref_auto_backup),
                    subtitle = "Backup otomatis ke Google Drive",
                    trailing = {
                        Switch(
                            checked = autoBackup,
                            onCheckedChange = { viewModel.setAutoBackup(it) }
                        )
                    }
                )
            }

            // About Section
            SettingsSection(
                title = stringResource(R.string.settings_about),
                icon = Icons.Default.Info
            ) {
                SettingRow(
                    title = "Versi Aplikasi",
                    subtitle = "1.0.0",
                    trailing = {}
                )
                SettingRow(
                    title = "Lisensi Open Source",
                    subtitle = "Lihat lisensi library yang digunakan",
                    trailing = {
                        TextButton(onClick = { /* show licenses */ }) {
                            Text("Lihat")
                        }
                    }
                )
                SettingRow(
                    title = "Lapor Masalah",
                    subtitle = "Kirim feedback atau lapor bug",
                    trailing = {
                        TextButton(onClick = { /* open github issues */ }) {
                            Text("Lapor")
                        }
                    }
                )
            }

            // Danger Zone
            SettingsSection(
                title = "Zona Bahaya",
                icon = Icons.Default.Delete
            ) {
                SettingRow(
                    title = "Hapus Semua Data",
                    subtitle = "Menghapus semua foto, PDF, dan database (TIDAK DAPAT DIBATALKAN)",
                    trailing = {
                        TextButton(
                            onClick = { /* show confirm dialog */ },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Hapus", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.material.icons.filled.Icon,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingRow(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}

@Composable
fun FlashModeSelector(
    selectedMode: Int,
    onModeChange: (Int) -> Unit
) {
    val modes = listOf(
        0 to "Auto",
        1 to "On",
        2 to "Off"
    )
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        modes.forEach { (mode, label) ->
            val isSelected = selectedMode == mode
            TextButton(
                onClick = { onModeChange(mode) },
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(label)
            }
        }
    }
}

@Composable
fun OrientationSelector(
    selectedOrientation: Int,
    onOrientationChange: (Int) -> Unit
) {
    val orientations = listOf(
        0 to "Potret",
        1 to "Lanskap"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        orientations.forEach { (orientation, label) ->
            val isSelected = selectedOrientation == orientation
            TextButton(
                onClick = { onOrientationChange(orientation) },
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(label)
            }
        }
    }
}

@Composable
fun PhotosPerPageSelector(
    selectedCount: Int,
    onCountChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (1..6).forEach { count ->
            val isSelected = selectedCount == count
            TextButton(
                onClick = { onCountChange(count) },
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(count.toString())
            }
        }
    }
}