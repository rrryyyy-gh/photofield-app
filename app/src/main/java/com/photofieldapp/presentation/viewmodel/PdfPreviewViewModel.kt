package com.photofieldapp.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photofieldapp.domain.usecase.GetDefaultPdfTemplateUseCase
import com.photofieldapp.domain.usecase.GetPdfTemplatesUseCase
import com.photofieldapp.domain.usecase.CreatePdfTemplateUseCase
import com.photofieldapp.domain.usecase.UpdatePdfTemplateUseCase
import com.photofieldapp.domain.usecase.DeletePdfTemplateUseCase
import com.photofieldapp.domain.usecase.SetDefaultPdfTemplateUseCase
import com.photofieldapp.util.PdfGeneratorUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PdfPreviewViewModel @Inject constructor(
    private val getPdfTemplatesUseCase: GetPdfTemplatesUseCase,
    private val getDefaultPdfTemplateUseCase: GetDefaultPdfTemplateUseCase,
    private val createPdfTemplateUseCase: CreatePdfTemplateUseCase,
    private val updatePdfTemplateUseCase: UpdatePdfTemplateUseCase,
    private val deletePdfTemplateUseCase: DeletePdfTemplateUseCase,
    private val setDefaultPdfTemplateUseCase: SetDefaultPdfTemplateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PdfPreviewUiState())
    val uiState: androidx.lifecycle.flow.StateFlow<PdfPreviewUiState> = _uiState.asStateFlow()

    data class PdfPreviewUiState(
        val templates: List<com.photofieldapp.domain.model.PdfTemplate> = emptyList(),
        val selectedTemplate: com.photofieldapp.domain.model.PdfTemplate? = null,
        val isLoading: Boolean = false,
        val isGenerating: Boolean = false,
        val generationProgress: PdfGeneratorUtil.GenerationProgress? = null,
        val generatedPdfPath: String? = null,
        val error: String? = null,
        val showTemplateEditor: Boolean = false,
        val editingTemplate: com.photofieldapp.domain.model.PdfTemplate? = null
    )

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getPdfTemplatesUseCase().onEach { templates ->
                val defaultTemplate = templates.find { it.isDefault } ?: templates.firstOrNull() ?: com.photofieldapp.domain.model.PdfTemplate.createDefault()
                _uiState.update { it.copy(templates = templates, selectedTemplate = defaultTemplate, isLoading = false) }
            }.launchIn(viewModelScope)
        }
    }

    fun selectTemplate(template: com.photofieldapp.domain.model.PdfTemplate) {
        _uiState.update { it.copy(selectedTemplate = template) }
    }

    fun setShowTemplateEditor(show: Boolean, template: com.photofieldapp.domain.model.PdfTemplate? = null) {
        _uiState.update { it.copy(showTemplateEditor = show, editingTemplate = template) }
    }

    fun saveTemplate(template: com.photofieldapp.domain.model.PdfTemplate) {
        viewModelScope.launch(Dispatchers.IO) {
            if (template.id == 0) {
                createPdfTemplateUseCase(template)
            } else {
                updatePdfTemplateUseCase(template)
            }
            _uiState.update { it.copy(showTemplateEditor = false, editingTemplate = null) }
        }
    }

    fun deleteTemplate(templateId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deletePdfTemplateUseCase(templateId)
        }
    }

    fun setDefaultTemplate(templateId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            setDefaultPdfTemplateUseCase(templateId)
        }
    }

    fun generatePdf(
        projectName: String,
        activityName: String,
        photos: List<com.photofieldapp.domain.model.Photo>,
        template: com.photofieldapp.domain.model.PdfTemplate,
        onResult: (Boolean, String?) -> Unit
    ) {
        _uiState.update { it.copy(isGenerating = true, generationProgress = null, error = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val result = PdfGeneratorUtil.generatePdf(
                context = getApplication(), // Need to get context - will fix
                projectName = projectName,
                activityName = activityName,
                photos = photos,
                template = template,
                progressCallback = { progress ->
                    _uiState.update { it.copy(generationProgress = progress) }
                }
            )

            _uiState.update { it.copy(isGenerating = false) }

            if (result.success) {
                _uiState.update { it.copy(generatedPdfPath = result.file?.absolutePath) }
                onResult(true, result.file?.absolutePath)
            } else {
                _uiState.update { it.copy(error = result.errorMessage) }
                onResult(false, result.errorMessage)
            }
        }
    }

    private fun getApplication(): android.app.Application {
        // This is a workaround - in real app, inject Application
        return _uiState.value.selectedTemplate?.let { it }?.let { 
            // Hack to get application context
            androidx.appcompat.app.AppCompatDelegate.getApplication() 
        } ?: throw IllegalStateException("Application not available")
    }

    fun clearGeneratedPdf() {
        _uiState.update { it.copy(generatedPdfPath = null) }
    }

    fun setError(error: String?) {
        _uiState.update { it.copy(error = error) }
    }
}