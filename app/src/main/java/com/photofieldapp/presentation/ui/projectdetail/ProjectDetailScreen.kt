package com.photofieldapp.presentation.ui.projectdetail

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.MoreVert
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
import com.photofieldapp.data.local.entity.Activity
import com.photofieldapp.presentation.viewmodel.ProjectDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProjectDetailScreen : androidx.activity.ComponentActivity() {
    private val viewModel: ProjectDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            com.photofieldapp.ui.theme.Theme.PhotoFieldApp {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProjectDetailContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ProjectDetailContent(
    viewModel: ProjectDetailViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.project_detail_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { /* navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* navigate to map */ }) {
                        Icon(Icons.Default.Map, contentDescription = stringResource(R.string.view_map))
                    }
                    IconButton(onClick = { /* navigate to PDF preview */ }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = stringResource(R.string.export_pdf))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setShowCreateDialog(true) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.btn_add_activity))
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
        ) {
            if (uiState.activities.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Folder,
                    title = stringResource(R.string.no_activities),
                    subtitle = stringResource(R.string.no_activities_desc),
                    actionLabel = stringResource(R.string.btn_add_activity),
                    onActionClick = { viewModel.setShowCreateDialog(true) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(0.dp, 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.activities) { activity ->
                        ActivityCard(
                            activity = activity,
                            photoCount = 0, // Would need to fetch from repo
                            onClick = { /* Navigate to activity detail */ },
                            onEdit = { viewModel.setEditingActivity(activity) },
                            onDelete = { viewModel.deleteActivity(activity.id) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        CreateActivityDialog(
            onDismiss = { viewModel.setShowCreateDialog(false); viewModel.setEditingActivity(null) },
            onCreate = { name, desc, locName, lat, lng ->
                viewModel.createActivity(name, desc, locName, lat, lng)
            },
            onUpdate = { activity ->
                viewModel.updateActivity(activity)
            },
            editingActivity = uiState.editingActivity
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
fun ActivityCard(
    activity: Activity,
    photoCount: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = stringResource(R.string.cd_activity_icon),
                    tint = MaterialTheme.colorScheme.secondary
                )
                if (photoCount > 0) {
                    Text(
                        text = photoCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .background(MaterialTheme.colorScheme.secondary, shape = CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (activity.description.isNotBlank()) {
                    Text(
                        text = activity.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (activity.locationName.isNotBlank()) {
                    Text(
                        text = "📍 ${activity.locationName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = stringResource(R.string.photo_count, photoCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MenuAnchor(onEdit = onEdit, onDelete = onDelete)
        }
    }
}

@Composable
fun MenuAnchor(onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(text = { Text(stringResource(R.string.btn_edit)) }, onClick = { onEdit(); expanded = false })
        DropdownMenuItem(
            text = { Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error) },
            onClick = { onDelete(); expanded = false }
        )
    }
    IconButton(onClick = { expanded = !expanded }) {
        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
    }
}

@Composable
fun EmptyState(
    icon: androidx.compose.material.icons.filled.Icon,
    title: String,
    subtitle: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            FilledButton(onClick = onActionClick) {
                Text(actionLabel)
            }
        }
    )
}

@Composable
fun CreateActivityDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Double, Double) -> Unit,
    onUpdate: (com.photofieldapp.data.local.entity.Activity) -> Unit,
    editingActivity: com.photofieldapp.data.local.entity.Activity?
) {
    var name by remember { mutableStateOf(editingActivity?.name ?: "") }
    var description by remember { mutableStateOf(editingActivity?.description ?: "") }
    var locationName by remember { mutableStateOf(editingActivity?.locationName ?: "") }

    val isEditing = editingActivity != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) stringResource(R.string.btn_edit) else stringResource(R.string.btn_add_activity)) },
        text = {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.project_name_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.project_desc_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text(stringResource(R.string.project_location_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        },
        dismissButton = {
            FilledButton(onClick = {
                if (name.isNotBlank()) {
                    if (isEditing) {
                        val updated = editingActivity.copyWithUpdated(
                            name = name,
                            description = description,
                            locationName = locationName
                        )
                        onUpdate(updated)
                    } else {
                        onCreate(name, description, locationName, 0.0, 0.0)
                    }
                }
            }) {
                Text(if (isEditing) stringResource(R.string.btn_save) else stringResource(R.string.btn_create))
            }
        }
    )
}