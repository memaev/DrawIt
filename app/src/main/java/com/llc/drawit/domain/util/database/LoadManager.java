package com.llc.drawit.domain.util.database;

public interface LoadManager<T> {
    void onResult(LoadData<T> data);
}
