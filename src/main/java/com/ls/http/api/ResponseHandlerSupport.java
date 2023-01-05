package com.ls.http.api;

import java.lang.reflect.Type;

public interface ResponseHandlerSupport {
  <T> ResponseHandler<T> getResponseHandler(Type type, String name);
  <T> boolean addResponseHandler(Type type, ResponseHandler<T> responseHandler);
  <T> void removeResponseHandler(Type type, ResponseHandler<T> responseHandler);
}
