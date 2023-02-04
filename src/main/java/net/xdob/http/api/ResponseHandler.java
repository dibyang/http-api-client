package net.xdob.http.api;

import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@FunctionalInterface
public interface  ResponseHandler<T> {
  T handleResponse(SimpleHttpResponse response) throws IOException, ParseException;
}
