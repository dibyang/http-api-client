package net.xdob.http.api;

import com.google.common.collect.Lists;
import com.ls.luava.common.N3Map;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.core5.concurrent.ComplexFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author yangzj
 * @date 2021/6/16
 */
public class ApiAsyncClientImpl implements ApiClient, RestClient, RestAsyncClient {

  public static final String E = "e";
  public static final String ERROR = "error";
  static Logger LOG = LoggerFactory.getLogger(ApiAsyncClientImpl.class);

  private final URI baseUri;

  private final List<RequestHandler> baseRequestHandlers;
  private final N3Map params = new N3Map();
  private final PostChecker checker;
  private final IFaceRoutePlanner routePlanner;
  private final HttpClient client;


  public ApiAsyncClientImpl(HttpClient client, IFaceRoutePlanner routePlanner, String baseUri, List<RequestHandler> requestHandlers, PostChecker checker) {
    this.client = client;
    this.routePlanner = routePlanner;
    this.baseUri = URI.create(baseUri);
    this.baseRequestHandlers = requestHandlers;
    this.checker = checker;
  }


  @Override
  public RestClient getRestClient() {
    return this;
  }

  @Override
  public RestAsyncClient getRestAsyncClient() {
    return this;
  }

  public List<RequestHandler> getBaseRequestHandles() {
    return baseRequestHandlers;
  }



  protected SimpleHttpResponse doRequest(String method, String uri, RequestHandler requestHandler) throws IOException, ExecutionException {

    try {
      return doAsyncRequest(method, uri, requestHandler,null).get();
    } catch (InterruptedException e) {
      LOG.warn(null,e);
    }
    return null;
  }

  @Override
  public Future<SimpleHttpResponse> doAsyncRequest(String method, String uri, RequestHandler requestHandler, FutureCallback<SimpleHttpResponse> callback) throws IOException {
    List<RequestHandler> requestHandlers = Lists.newArrayList(baseRequestHandlers);

    final SimpleRequestBuilder requestBuilder = SimpleRequestBuilder.create(method)
        .setUri(baseUri.resolve(uri));
    if(requestHandler !=null){
      requestHandlers.add(requestHandler);
    }
    for (RequestHandler handle : requestHandlers) {
      handle.handle(requestBuilder);
    }
    SimpleHttpRequest httpRequest = requestBuilder.build();
    Future<SimpleHttpResponse> future = client.getHttpAsyncClient().execute(httpRequest, callback);
    return future;

  }

  @Override
  public <T> Future<T> doAsyncRequest(String method, String uri, RequestHandler requestHandler, ResponseHandler<T> responseHandler, FutureCallback<T> callback) {
    final ComplexFuture<T> future = new ComplexFuture<>(callback);
    FutureCallback<SimpleHttpResponse> futureCallback = new FutureCallback<SimpleHttpResponse>() {

      @Override
      public void completed(SimpleHttpResponse result) {
        T data = null;
        try {
          data = responseHandler.handleResponse(result);
        } catch (IOException e) {
          e.printStackTrace();
        } catch (ParseException e) {
          e.printStackTrace();
        }
        future.completed(data);
      }

      @Override
      public void failed(final Exception ex) {
        future.failed(ex);
      }

      @Override
      public void cancelled() {
        future.cancel();
      }
    };
    try {
      doAsyncRequest(method, uri, requestHandler,
          futureCallback);
    } catch (IOException e) {
      futureCallback.failed(e);
    }
    return future;
  }

  @Override
  public <T> Future<T> doAsyncRequest(String method, String uri, Map<String, Object> params, ResponseHandler<T> responseHandler, FutureCallback<T> callback) {
    return doAsyncRequest(method, uri, getParamsHandle(params), responseHandler, callback);
  }

  @Override
  public Future<N3Map> asyncRequest(String method, String uri, RequestHandler requestHandler, FutureCallback<N3Map> callback) {
    return doAsyncRequest(method, uri, requestHandler, new N3MapResponseHandler(), callback);
  }

  @Override
  public Future<N3Map> asyncRequest(String method, String uri, Map<String, Object> params, FutureCallback<N3Map> callback) {
    return doAsyncRequest(method, uri, getParamsHandle(params), new N3MapResponseHandler(), callback);
  }

  @Override
  public Future<N3Map> asyncRequest(String method, String uri, FutureCallback<N3Map> callback) {
    return doAsyncRequest(method, uri, (RequestHandler)null, new N3MapResponseHandler(), callback);
  }

  @Override
  public Future<N3Map> asyncGet(String uri, Map<String, Object> params, FutureCallback<N3Map> callback) {
    return doAsyncRequest(HttpMethod.GET.name(), uri, getParamsHandle(params), new N3MapResponseHandler(), callback);
  }

  @Override
  public Future<N3Map> asyncPost(String uri, Map<String, Object> params, FutureCallback<N3Map> callback) {
    return doAsyncRequest(HttpMethod.POST.name(), uri, getParamsHandle(params), new N3MapResponseHandler(), callback);
  }

  @Override
  public Future<N3Map> asyncPut(String uri, Map<String, Object> params, FutureCallback<N3Map> callback) {
    return doAsyncRequest(HttpMethod.PUT.name(), uri, getParamsHandle(params), new N3MapResponseHandler(), callback);
  }

  @Override
  public Future<N3Map> asyncDelete(String uri, Map<String, Object> params, FutureCallback<N3Map> callback) {
    return doAsyncRequest(HttpMethod.DELETE.name(), uri, getParamsHandle(params), new N3MapResponseHandler(), callback);
  }

  @Override
  public Future<N3Map> asyncGet(String uri, RequestHandler requestHandler, FutureCallback<N3Map> callback) {
    return doAsyncRequest(HttpMethod.GET.name(), uri, requestHandler, new N3MapResponseHandler(), callback);
  }

  @Override
  public Future<N3Map> asyncPost(String uri, RequestHandler requestHandler, FutureCallback<N3Map> callback) {
    return doAsyncRequest(HttpMethod.POST.name(), uri, requestHandler, new N3MapResponseHandler(), callback);
  }

  @Override
  public Future<N3Map> asyncPut(String uri, RequestHandler requestHandler, FutureCallback<N3Map> callback) {
    return doAsyncRequest(HttpMethod.PUT.name(), uri, requestHandler, new N3MapResponseHandler(), callback);
  }

  @Override
  public Future<N3Map> asyncDelete(String uri, RequestHandler requestHandler, FutureCallback<N3Map> callback) {
    return doAsyncRequest(HttpMethod.DELETE.name(), uri, requestHandler, new N3MapResponseHandler(), callback);
  }

  @Override
  public Future<N3Map> asyncGet(String uri, FutureCallback<N3Map> callback) {
    return doAsyncRequest(HttpMethod.GET.name(), uri, (RequestHandler)null, new N3MapResponseHandler(), callback);
  }

  @Override
  public Future<N3Map> asyncPost(String uri, FutureCallback<N3Map> callback) {
    return doAsyncRequest(HttpMethod.POST.name(), uri, (RequestHandler)null, new N3MapResponseHandler(), callback);
  }

  @Override
  public Future<N3Map> asyncPut(String uri, FutureCallback<N3Map> callback) {
    return doAsyncRequest(HttpMethod.PUT.name(), uri, (RequestHandler)null, new N3MapResponseHandler(), callback);
  }

  @Override
  public Future<N3Map> asyncDelete(String uri, FutureCallback<N3Map> callback) {
    return doAsyncRequest(HttpMethod.DELETE.name(), uri, (RequestHandler)null, new N3MapResponseHandler(), callback);
  }


  @Override
  public <T> T doRequest(String method, String uri, RequestHandler requestHandler, ResponseHandler<T> responseHandler) throws IOException, ParseException, ExecutionException {
    SimpleHttpResponse response = doRequest(method,uri, requestHandler);
    return responseHandler.handleResponse(response);
  }

  @Override
  public N3Map request(String method, String uri, RequestHandler requestHandler) {
    N3Map n3Map = new N3Map();
    try {
      N3Map data = doRequest(method,uri, requestHandler,new N3MapResponseHandler());
      n3Map.putAll(data);
    } catch (IOException | ParseException | ExecutionException e) {
      n3Map.put(EXCEPTION,e);
      n3Map.put(E,e);
      n3Map.put(ERROR,e.getClass().getSimpleName());
      //LOG.warn(null,e);
    }
    if(checker!=null&&checker.postCheck(this,n3Map)){
      n3Map = request(method, uri, requestHandler);
    }
    return n3Map;
  }


  protected SimpleHttpResponse doRequest(String method, String uri, Map<String, Object> params) throws IOException,ExecutionException {
    return doRequest(method,uri,getParamsHandle(params));
  }

  public RequestHandler getParamsHandle(Map<String, Object> params) {
    return builder -> {
      if(params!=null) {
        for (String name : params.keySet()) {
          Object value = handleValue(params.get(name));
          if (value instanceof Iterable) {
            Iterator iter = ((Iterable) value).iterator();
            while (iter.hasNext()) {
              Object o = iter.next();
              if (o != null) {
                builder.addParameter(name, String.valueOf(o));
              }
            }
          } else {
            builder.addParameter(name, String.valueOf(value));
          }
        }
      }
    };
  }

  private Object handleValue(Object value) {
    if (value != null && value.getClass().isArray()) {
      value = Arrays.asList((Object[]) value);
    }
    if (value instanceof Date) {
      value = ((Date) value).getTime();
    }
    return value;
  }

  @Override
  public <T> T doRequest(String method, String uri, Map<String, Object> params, ResponseHandler<T> responseHandler) throws IOException, ParseException, ExecutionException {
    SimpleHttpResponse response = doRequest(method,uri, params);
    return responseHandler.handleResponse(response);
  }


  protected SimpleHttpResponse doRequest(HttpMethod method, String uri, RequestHandler requestHandler) throws IOException, ExecutionException {
    return doRequest(method.name(),uri, requestHandler);
  }



  protected SimpleHttpResponse doRequest(HttpMethod method, String uri, Map<String, Object> params) throws IOException, ExecutionException {
    return doRequest(method.name(),uri,params);
  }

  @Override
  public N3Map request(String method, String uri, Map<String, Object> params) {
    return request(method,uri,getParamsHandle(params));
  }

  @Override
  public N3Map request(String method, String uri) {
    return request(method,uri,(RequestHandler)null);
  }

  @Override
  public N3Map get(String uri, Map<String, Object> params) {
    return request(HttpGet.METHOD_NAME,uri,getParamsHandle(params));
  }

  @Override
  public N3Map post(String uri, Map<String, Object> params) {
    return request(HttpPost.METHOD_NAME,uri,getParamsHandle(params));
  }

  @Override
  public N3Map put(String uri, Map<String, Object> params) {
    return request(HttpPut.METHOD_NAME,uri,getParamsHandle(params));
  }

  @Override
  public N3Map delete(String uri, Map<String, Object> params) {
    return request(HttpDelete.METHOD_NAME,uri,getParamsHandle(params));
  }

  @Override
  public N3Map get(String uri, RequestHandler requestHandler) {
    return request(HttpGet.METHOD_NAME,uri, requestHandler);
  }

  @Override
  public N3Map post(String uri, RequestHandler requestHandler) {
    return request(HttpPost.METHOD_NAME,uri, requestHandler);
  }

  @Override
  public N3Map put(String uri, RequestHandler requestHandler) {
    return request(HttpPut.METHOD_NAME,uri, requestHandler);
  }

  @Override
  public N3Map delete(String uri, RequestHandler requestHandler) {
    return request(HttpDelete.METHOD_NAME,uri, requestHandler);
  }

  @Override
  public N3Map get(String uri) {
    return request(HttpGet.METHOD_NAME,uri);
  }

  @Override
  public N3Map post(String uri) {
    return request(HttpPost.METHOD_NAME,uri);
  }

  @Override
  public N3Map put(String uri) {
    return request(HttpPut.METHOD_NAME,uri);
  }

  @Override
  public N3Map delete(String uri) {
    return request(HttpDelete.METHOD_NAME,uri);
  }

  @Override
  public <T> T getApiProxy(Class<T> clazz) {
    ClientProxy<T> proxy = new ClientProxy<T>(this,clazz);
    return proxy.getInstance();
  }

  @Override
  public void setIface(String iface) {
    routePlanner.setIface(iface);
  }

  @Override
  public String getIface() {
    return routePlanner.getIface();
  }


  @Override
  public N3Map getParams() {
    return params;
  }

  @Override
  public HttpClient getHttpClient() {
    return client;
  }


}
