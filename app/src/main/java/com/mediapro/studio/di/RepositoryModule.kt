package com.mediapro.studio.di
import android.content.Context
import androidx.room.Room
import com.mediapro.studio.data.local.MediaProDatabase
import com.mediapro.studio.data.local.ProjectDao
import com.mediapro.studio.data.repository.RoomProjectRepository
import com.mediapro.studio.domain.repository.ProjectRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MediaProDatabase =
        Room.databaseBuilder(context, MediaProDatabase::class.java, "mediapro_studio.db").build()
    @Provides
    fun provideProjectDao(database: MediaProDatabase): ProjectDao = database.projectDao()
}
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindProjectRepository(implementation: RoomProjectRepository): ProjectRepository
}
