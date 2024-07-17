package net.xdob.http.api;

import com.ls.luava.common.N3Map;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public interface RestAsyncClient {
  //FutureCallback<SimpleHttpResponse> callback
  CompletableFuture<SimpleHttpResponse> doAsyncRequest(String method, String uri, RequestHandler requestHandler) throws IOException;

  <T> CompletableFuture<T> doAsyncRequest(String method, String uri, RequestHandler requestHandler, final ResponseHandler<T> responseHandler);
  <T> CompletableFuture<T> doAsyncRequest(String method, String uri, Map<String,Object> params, final ResponseHandler<T> responseHandler);

  CompletableFuture<N3Map> asyncRequest(String method, String uri, RequestHandler requestHandler);
  CompletableFuture<N3Map> asyncRequest(String method, String uri, Map<String,Object> params);
  CompletableFuture<N3Map> asyncRequest(String method, String uri);

  CompletableFuture<N3Map> asyncGet(String uri, Map<String,Object> params);
  CompletableFuture<N3Map> asyncPost(String uri, Map<String,Object> params);
  CompletableFuture<N3Map> asyncPut(String uri, Map<String,Object> params);
  CompletableFuture<N3Map> asyncDelete(String uri, Map<String,Object> params);
  CompletableFuture<N3Map> asyncGet(String uri, RequestHandler requestHandler);
  CompletableFuture<N3Map> asyncPost(String uri, RequestHandler requestHandler);
  CompletableFuture<N3Map> asyncPut(String uri, RequestHandler requestHandler);
  CompletableFuture<N3Map> asyncDelete(String uri, RequestHandler requestHandler);
  CompletableFuture<N3Map> asyncGet(String uri);
  CompletableFuture<N3Map> asyncPost(String uri);
  CompletableFuture<N3Map> asyncPut(String uri);
  CompletableFuture<N3Map> asyncDelete(String uri);

}
