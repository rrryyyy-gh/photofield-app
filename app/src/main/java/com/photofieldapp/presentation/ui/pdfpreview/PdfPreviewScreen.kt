package com.photofieldapp.presentation.ui.pdfpreview

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.photofieldapp.R
import com.photofieldapp.domain.model.PdfTemplate
import com.photofieldapp.presentation.viewmodel.PdfPreviewViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PdfPreviewScreen : androidx.activity.ComponentActivity() {
    private val viewModel: PdfPreviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            com.photofieldapp.ui.theme.Theme.PhotoFieldApp {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PdfPreviewContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PdfPreviewContent(
    viewModel: PdfPreviewViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pdf_preview_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { /* navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.generatedPdfPath != null) {
                        IconButton(onClick = { /* share PDF */ }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.pdf_share))
                        }
                        IconButton(onClick = { /* save PDF */ }) {
                            Icon(Icons.Default.FileDownload, contentDescription = stringResource(R.string.pdf_save))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Template Selector
            TemplateSelectorSection(
                templates = uiState.templates,
                selectedTemplate = uiState.selectedTemplate,
                onTemplateClick = { viewModel.selectTemplate(it) },
                onAddTemplate = { viewModel.setShowTemplateEditor(true) },
                onEditTemplate = { viewModel.setShowTemplateEditor(true, it) },
                onDeleteTemplate = { viewModel.deleteTemplate(it.id) },
                onSetDefault = { viewModel.setDefaultTemplate(it.id) }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Preview Area
            if (uiState.generatedPdfPath != null) {
                PdfPreviewArea(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    pdfPath = uiState.generatedPdfPath!!,
                    onRegenerate = { /* regenerate with current template */ },
                    onShare = { /* share */ },
                    onSave = { /* save */ }
                )
            } else {
                EmptyPreviewState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    onGenerate = { /* generate with selected template */ }
                )
            }
        }
    }

    if (uiState.showTemplateEditor) {
        TemplateEditorDialog(
            onDismiss = { viewModel.setShowTemplateEditor(false) },
            template = uiState.editingTemplate,
            onSave = { template -> viewModel.saveTemplate(template) }
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
fun TemplateSelectorSection(
    templates: List<PdfTemplate>,
    selectedTemplate: PdfTemplate?,
    onTemplateClick: (PdfTemplate) -> Unit,
    onAddTemplate: () -> Unit,
    onEditTemplate: (PdfTemplate) -> Unit,
    onDeleteTemplate: (PdfTemplate) -> Unit,
    onSetDefault: (PdfTemplate) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.pdf_template_title), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onAddTemplate) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Text("Template Baru")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(templates) { template ->
                TemplateCard(
                    template = template,
                    isSelected = template.id == selectedTemplate?.id,
                    isDefault = template.isDefault,
                    onClick = { onTemplateClick(template) },
                    onEdit = { onEditTemplate(template) },
                    onDelete = { onDeleteTemplate(template) },
                    onSetDefault = { onSetDefault(template) }
                )
            }
        }
    }
}

@Composable
fun TemplateCard(
    template: PdfTemplate,
    isSelected: Boolean,
    isDefault: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            ),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio indicator
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Default.RadioButtonUnchecked, contentDescription = "Not selected", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(template.name, style = MaterialTheme.typography.titleSmall)
                    if (isDefault) {
                        Badge() {
                            Text("Default", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Text(
                    text = "${template.photosPerPage} foto/halaman • ${if (template.pageOrientation == 0) "Potret" else "Lanskap"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Menu
            var expanded by remember { mutableStateOf(false) }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(text = { Text(stringResource(R.string.btn_edit)) }, onClick = { onEdit(); expanded = false })
                if (!isDefault) {
                    DropdownMenuItem(text = { Text("Jadikan Default") }, onClick = { onSetDefault(); expanded = false })
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error) },
                    onClick = { onDeleteTemplate(template); expanded = false }
                )
            }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
        }
    }
}

@Composable
fun PdfPreviewArea(
    modifier: Modifier = Modifier,
    pdfPath: String,
    onRegenerate: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clip(RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.padding(32.dp))
        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Text("PDF Siap Diunduh", style = MaterialTheme.typography.titleMedium)
        Text(pdfPath, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)

        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledButton(onClick = onRegenerate) {
                Text("Buat Ulang")
            }
            OutlinedButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Share")
                Text("Bagikan")
            }
            OutlinedButton(onClick = onSave) {
                Icon(Icons.Default.FileDownload, contentDescription = "Save")
                Text("Simpan")
            }
        }
        Spacer(modifier = Modifier.padding(32.dp))
    }
}

@Composable
fun EmptyPreviewState(
    modifier: Modifier = Modifier,
    onGenerate: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Text("Belum Ada PDF", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Pilih template dan tekan tombol generate untuk membuat PDF", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            FilledButton(onClick = onGenerate) {
                Text("Generate PDF")
            }
        }
    }
}

@Composable
fun TemplateEditorDialog(
    onDismiss: () -> Unit,
    template: PdfTemplate?,
    onSave: (PdfTemplate) -> Unit
) {
    val isEditing = template != null
    var name by remember { mutableStateOf(template?.name ?: "") }
    var headerText by remember { mutableStateOf(template?.headerText ?: "") }
    var footerText by remember { mutableStateOf(template?.footerText ?: "") }
    var watermarkText by remember { mutableStateOf(template?.watermarkText ?: "") }
    var photosPerPage by remember { mutableStateOf(template?.photosPerPage ?: 2) }
    var pageOrientation by remember { mutableStateOf(template?.pageOrientation ?: 0) }
    var includeGps by remember { mutableStateOf(template?.includeGps ?: true) }
    var includeTimestamp by remember { mutableStateOf(template?.includeTimestamp ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Template" else "Template Baru") },
        text = {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().width(300.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Template") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = headerText,
                    onValueChange = { headerText = it },
                    label = { Text("Header (gunakan {project}, {activity}, {page}, {total}, {date})") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = footerText,
                    onValueChange = { footerText = it },
                    label = { Text("Footer (gunakan {page}, {total}, {date})") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = watermarkText,
                    onValueChange = { watermarkText = it },
                    label = { Text("Watermark (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Photos per page dropdown
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Foto per Halaman", style = MaterialTheme.typography.labelSmall)
                        DropdownMenu(
                            expanded = false, // Simplified
                            onDismissRequest = {}
                        ) { }
                        OutlinedTextField(
                            value = photosPerPage.toString(),
                            onValueChange = { photosPerPage = it.toIntOrNull() ?: 2 },
                            label = { Text("$photosPerPage") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                    }
                    
                    // Orientation dropdown
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Orientasi", style = MaterialTheme.typography.labelSmall)
                        OutlinedTextField(
                            value = if (pageOrientation == 0) "Potret" else "Lanskap",
                            onValueChange = { pageOrientation = if (it == "Lanskap") 1 else 0 },
                            label = { Text("") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            readOnly = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Checkbox(
                            checked = includeGps,
                            onCheckedChange = { includeGps = it }
                        )
                        Text("Sertakan GPS")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Checkbox(
                            checked = includeTimestamp,
                            onCheckedChange = { includeTimestamp = it }
                        )
                        Text("Sertakan Waktu")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        },
        dismissButton = {
            FilledButton(onClick = {
                if (name.isNotBlank()) {
                    val savedTemplate = if (isEditing) {
                        template!!.copyWithUpdated(
                            name = name,
                            headerText = headerText,
                            footerText = footerText,
                            watermarkText = watermarkText,
                            photosPerPage = photosPerPage,
                            pageOrientation = pageOrientation,
                            includeGps = includeGps,
                            includeTimestamp = includeTimestamp
                        )
                    } else {
                        com.photofieldapp.domain.model.PdfTemplate(
                            name = name,
                            headerText = headerText,
                            footerText = footerText,
                            watermarkText = watermarkText,
                            photosPerPage = photosPerPage,
                            pageOrientation = pageOrientation,
                            includeGps = includeGps,
                            includeTimestamp = includeTimestamp
                        )
                    }
                    onSave(savedTemplate)
                }
            }) {
                Text(stringResource(R.string.btn_save))
            }
        }
    )
}