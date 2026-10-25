package com.photofieldapp.domain.usecase

import com.photofieldapp.data.repository.PdfTemplateRepository
import com.photofieldapp.domain.model.PdfTemplate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPdfTemplatesUseCase @Inject constructor(
    private val repository: PdfTemplateRepository
) {
    operator fun invoke(): Flow<List<PdfTemplate>> = repository.getAllTemplates()
}

class GetDefaultPdfTemplateUseCase @Inject constructor(
    private val repository: PdfTemplateRepository
) {
    operator fun invoke(): PdfTemplate? = repository.getDefaultTemplate()
}

class CreatePdfTemplateUseCase @Inject constructor(
    private val repository: PdfTemplateRepository
) {
    operator fun invoke(template: PdfTemplate): Long = repository.createTemplate(template)
}

class UpdatePdfTemplateUseCase @Inject constructor(
    private val repository: PdfTemplateRepository
) {
    operator fun invoke(template: PdfTemplate) = repository.updateTemplate(template)
}

class DeletePdfTemplateUseCase @Inject constructor(
    private val repository: PdfTemplateRepository
) {
    operator fun invoke(templateId: Long) = repository.deleteTemplate(templateId)
}

class SetDefaultPdfTemplateUseCase @Inject constructor(
    private val repository: PdfTemplateRepository
) {
    operator fun invoke(templateId: Long) = repository.setDefaultTemplate(templateId)
}