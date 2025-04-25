package com.llc.drawit.di;

import com.llc.drawit.data.repositoryImpl.StorageRepositoryImpl;
import com.llc.drawit.data.repositoryImpl.UserRepositoryImpl;
import com.llc.drawit.data.repositoryImpl.WhiteboardRepositoryImpl;
import com.llc.drawit.domain.repository.StorageRepository;
import com.llc.drawit.domain.repository.UserRepository;
import com.llc.drawit.domain.repository.WhiteboardRepository;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * This class is Dagger module to declare @Provides functions for resolving dependencies
 */
@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {
    @Provides
    @Singleton
    public UserRepository provideUserRepository(Lazy<WhiteboardRepository> whiteboardRepositoryLazy){
        return new UserRepositoryImpl(whiteboardRepositoryLazy);
    }

    @Provides
    @Singleton
    public StorageRepository provideStorageRepository(){
        return new StorageRepositoryImpl();
    }

    @Provides
    @Singleton
    public WhiteboardRepository provideWhiteboardRepository(Lazy<UserRepository> userRepositoryLazy){
        return new WhiteboardRepositoryImpl(userRepositoryLazy);
    }
}
