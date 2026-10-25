package com.photofieldapp.di

import android.content.Context
import com.photofieldapp.data.local.AppDatabase
import com.photofieldapp.data.local.dao.ActivityDao
import com.photofieldapp.data.local.dao.PdfTemplateDao
import com.photofieldapp.data.local.dao.PhotoDao
import com.photofieldapp.data.local.dao.ProjectDao
import com.photofieldapp.data.repository.ActivityRepository
import com.photofieldapp.data.repository.ActivityRepositoryImpl
import com.photofieldapp.data.repository.PdfTemplateRepository
import com.photofieldapp.data.repository.PdfTemplateRepositoryImpl
import com.photofieldapp.data.repository.PhotoRepository
import com.photofieldapp.data.repository.PhotoRepositoryImpl
import com.photofieldapp.data.repository.ProjectRepository
import com.photofieldapp.data.repository.ProjectRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@Suppress("UNUSED_PARAMETER") context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideProjectDao(database: AppDatabase): ProjectDao = database.projectDao()

    @Provides
    fun provideActivityDao(database: AppDatabase): ActivityDao = database.activityDao()

    @Provides
    fun providePhotoDao(database: AppDatabase): PhotoDao = database.photoDao()

    @Provides
    fun providePdfTemplateDao(database: AppDatabase): PdfTemplateDao = database.pdfTemplateDao()

    @Provides
    @Singleton
    fun provideProjectRepository(dao: ProjectDao): ProjectRepository = ProjectRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideActivityRepository(dao: ActivityDao): ActivityRepository = ActivityRepositoryImpl(dao)

    @Provides
    @Singleton
    fun providePhotoRepository(dao: PhotoDao): PhotoRepository = PhotoRepositoryImpl(dao)

    @Provides
    @Singleton
    fun providePdfTemplateRepository(dao: PdfTemplateDao): PdfTemplateRepository = PdfTemplateRepositoryImpl(dao)
}