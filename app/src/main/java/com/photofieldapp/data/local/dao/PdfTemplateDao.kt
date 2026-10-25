package com.photofieldapp.data.local.dao

import androidx.room.*
import com.photofieldapp.data.local.entity.PdfTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfTemplateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: PdfTemplate): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<PdfTemplate>)

    @Update
    suspend fun update(template: PdfTemplate): Int

    @Delete
    suspend fun delete(template: PdfTemplate): Int

    @Query("DELETE FROM pdf_templates WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM pdf_templates ORDER BY isDefault DESC, name ASC")
    fun getAllTemplates(): Flow<List<PdfTemplate>>

    @Query("SELECT * FROM pdf_templates ORDER BY isDefault DESC, name ASC")
    suspend fun getAllTemplatesOnce(): List<PdfTemplate>

    @Query("SELECT * FROM pdf_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): PdfTemplate?

    @Query("SELECT * FROM pdf_templates WHERE isDefault = 1 LIMIT 1")
    fun getDefaultTemplate(): Flow<PdfTemplate?>

    @Query("SELECT * FROM pdf_templates WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultTemplateOnce(): PdfTemplate?

    @Query("UPDATE pdf_templates SET isDefault = 0 WHERE isDefault = 1")
    suspend fun clearDefault(): Int

    @Query("UPDATE pdf_templates SET isDefault = 1 WHERE id = :id")
    suspend fun setDefault(id: Long): Int
}