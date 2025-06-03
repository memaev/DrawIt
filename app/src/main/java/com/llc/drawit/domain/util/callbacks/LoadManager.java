package com.llc.drawit.domain.util.callbacks;

import com.llc.drawit.domain.util.database.LoadData;

public interface LoadManager<T> {
    void onResult(LoadData<T> data);
}
