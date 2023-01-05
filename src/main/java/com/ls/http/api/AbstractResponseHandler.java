package com.ls.http.api;

import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

public abstract class AbstractResponseHandler<T> implements ResponseHandler<T> {

  public abstract T handleBody(SimpleBody body) throws IOException, ParseException;

  @Override
  public T handleResponse(SimpleHttpResponse response) throws IOException, ParseException {
    return handleBody(response.getBody());
  }
}
