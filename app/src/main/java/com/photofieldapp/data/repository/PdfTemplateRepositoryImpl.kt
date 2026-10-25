package com.photofieldapp.data.repository

import com.photofieldapp.data.local.dao.PdfTemplateDao
import com.photofieldapp.data.local.entity.PdfTemplate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfTemplateRepositoryImpl @Inject constructor(
    private val pdfTemplateDao: PdfTemplateDao
) : PdfTemplateRepository {

    override suspend fun createTemplate(template: PdfTemplate): Long {
        // If this is set as default, clear existing default first
        if (template.isDefault) {
            pdfTemplateDao.clearDefault()
        }
        return pdfTemplateDao.insert(template)
    }

    override suspend fun updateTemplate(template: PdfTemplate) {
        if (template.isDefault) {
            pdfTemplateDao.clearDefault()
        }
        pdfTemplateDao.update(template)
    }

    override suspend fun deleteTemplate(templateId: Long) {
        pdfTemplateDao.deleteById(templateId)
    }

    override fun getAllTemplates(): Flow<List<PdfTemplate>> {
        return pdfTemplateDao.getAllTemplates()
    }

    override suspend fun getDefaultTemplate(): PdfTemplate? {
        return pdfTemplateDao.getDefaultTemplateOnce()
    }

    override suspend fun setDefaultTemplate(templateId: Long) {
        pdfTemplateDao.clearDefault()
        pdfTemplateDao.setDefault(templateId)
    }
}