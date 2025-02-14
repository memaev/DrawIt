package com.llc.drawit.di;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

// Этот класс является модулем Dagger, который предоставляет зависимости для всего приложения
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    @Provides
    @Singleton
    public Context provideApplicationContext(Application app){
        return app.getApplicationContext();
    }
}
