package com.mta.location.util;

public interface ResponseHandler<T> {

    void onResponse(T data);
}
