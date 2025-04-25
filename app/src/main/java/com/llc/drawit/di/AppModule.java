package com.llc.drawit.di;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * This class is Dagger module to declare @Provides functions for resolving dependencies
 */
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    @Provides
    @Singleton
    public Context provideApplicationContext(Application app){
        return app.getApplicationContext();
    }
}
