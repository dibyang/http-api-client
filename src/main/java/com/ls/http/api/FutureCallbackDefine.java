package com.ls.http.api;

import org.apache.hc.core5.concurrent.FutureCallback;

import java.lang.reflect.Type;

public class FutureCallbackDefine {
  private final FutureCallback callback;
  private Type dataType;

  public FutureCallbackDefine(FutureCallback callback) {
    this.callback = callback;
  }

  public FutureCallback getCallback() {
    return callback;
  }

  public Type getDataType() {
    return dataType;
  }

  public void setDataType(Type dataType) {
    this.dataType = dataType;
  }
}
