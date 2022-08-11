package com.ls.http.api;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

public interface  ResponseHandler<T> {
  T handleResponse(CloseableHttpResponse response) throws IOException, ParseException;
}
