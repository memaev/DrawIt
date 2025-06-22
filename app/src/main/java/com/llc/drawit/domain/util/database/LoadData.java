package com.llc.drawit.domain.util.database;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadData<T>{
    private Result resultCode;
    private T data;

    public LoadData(Result resultCode, T data){
        this.resultCode = resultCode;
        this.data = data;
    }
}
