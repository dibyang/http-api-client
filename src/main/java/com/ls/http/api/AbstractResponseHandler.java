package com.ls.http.api;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

public abstract class AbstractResponseHandler<T> implements ResponseHandler<T> {

  public abstract T handleEntity(HttpEntity entity) throws IOException, ParseException;

  @Override
  public T handleResponse(CloseableHttpResponse response) throws IOException, ParseException {
    return handleEntity(response.getEntity());
  }
}
