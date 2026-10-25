package com.photofieldapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.photofieldapp.data.local.converter.DateTimeConverter
import com.photofieldapp.data.local.converter.ListConverter
import com.photofieldapp.data.local.dao.ActivityDao
import com.photofieldapp.data.local.dao.PdfTemplateDao
import com.photofieldapp.data.local.dao.PhotoDao
import com.photofieldapp.data.local.dao.ProjectDao
import com.photofieldapp.data.local.entity.Activity
import com.photofieldapp.data.local.entity.Photo
import com.photofieldapp.data.local.entity.Project
import com.photofieldapp.data.local.entity.PdfTemplate

@Database(
    entities = [
        Project::class,
        Activity::class,
        Photo::class,
        PdfTemplate::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateTimeConverter::class, ListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun activityDao(): ActivityDao
    abstract fun photoDao(): PhotoDao
    abstract fun pdfTemplateDao(): PdfTemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "photofield.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}