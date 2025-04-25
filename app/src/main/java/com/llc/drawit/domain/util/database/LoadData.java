package com.llc.drawit.domain.util.database;

public class LoadData<T>{
    private Result resultCode;
    private T data;

    public LoadData(Result resultCode, T data){
        this.resultCode = resultCode;
        this.data = data;
    }

    public Result getResultCode() {
        return resultCode;
    }

    public void setResultCode(Result resultCode) {
        this.resultCode = resultCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
