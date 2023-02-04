package net.xdob.http.api;

import com.ls.luava.common.N3Map;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;

public interface RestAsyncClient {
  //FutureCallback<SimpleHttpResponse> callback
  Future<SimpleHttpResponse> doAsyncRequest(String method, String uri, RequestHandler requestHandler, final FutureCallback<SimpleHttpResponse> callback) throws IOException;

  <T> Future<T> doAsyncRequest(String method, String uri, RequestHandler requestHandler, final ResponseHandler<T> responseHandler, final FutureCallback<T> callback);
  <T> Future<T> doAsyncRequest(String method, String uri, Map<String,Object> params, final ResponseHandler<T> responseHandler, final FutureCallback<T> callback);

  Future<N3Map> asyncRequest(String method, String uri, RequestHandler requestHandler, final FutureCallback<N3Map> callback);
  Future<N3Map> asyncRequest(String method, String uri, Map<String,Object> params, final FutureCallback<N3Map> callback);
  Future<N3Map> asyncRequest(String method, String uri, final FutureCallback<N3Map> callback);

  Future<N3Map> asyncGet(String uri, Map<String,Object> params, final FutureCallback<N3Map> callback);
  Future<N3Map> asyncPost(String uri, Map<String,Object> params, final FutureCallback<N3Map> callback);
  Future<N3Map> asyncPut(String uri, Map<String,Object> params, final FutureCallback<N3Map> callback);
  Future<N3Map> asyncDelete(String uri, Map<String,Object> params, final FutureCallback<N3Map> callback);
  Future<N3Map> asyncGet(String uri, RequestHandler requestHandler, final FutureCallback<N3Map> callback);
  Future<N3Map> asyncPost(String uri, RequestHandler requestHandler, final FutureCallback<N3Map> callback);
  Future<N3Map> asyncPut(String uri, RequestHandler requestHandler, final FutureCallback<N3Map> callback);
  Future<N3Map> asyncDelete(String uri, RequestHandler requestHandler, final FutureCallback<N3Map> callback);
  Future<N3Map> asyncGet(String uri, final FutureCallback<N3Map> callback);
  Future<N3Map> asyncPost(String uri, final FutureCallback<N3Map> callback);
  Future<N3Map> asyncPut(String uri, final FutureCallback<N3Map> callback);
  Future<N3Map> asyncDelete(String uri, final FutureCallback<N3Map> callback);

}
