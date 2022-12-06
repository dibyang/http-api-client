package com.ls.http.api;

public interface ResponseHandlerSupport {
  <T> ResponseHandler<T> getResponseHandler(Class<T> type,String name);
  <T> boolean addResponseHandler(Class<T> type, ResponseHandler<T> responseHandler);
  <T> void removeResponseHandler(Class<T> type, ResponseHandler<T> responseHandler);
}
