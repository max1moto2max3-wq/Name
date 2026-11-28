package com.mediapro.studio.di
import com.mediapro.studio.data.repository.InMemoryProjectRepository
import com.mediapro.studio.domain.repository.ProjectRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindProjectRepository(implementation: InMemoryProjectRepository): ProjectRepository
}
