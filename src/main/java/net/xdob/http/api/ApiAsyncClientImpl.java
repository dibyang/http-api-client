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
import java.util.concurrent.CompletableFuture;
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



  protected SimpleHttpResponse doRequest(String method, String uri, RequestHandler requestHandler) throws IOException, ExecutionException, InterruptedException {
    return doAsyncRequest(method, uri, requestHandler).get();

  }

  @Override
  public CompletableFuture<SimpleHttpResponse> doAsyncRequest(String method, String uri, RequestHandler requestHandler) throws IOException {
    List<RequestHandler> requestHandlers = Lists.newArrayList(baseRequestHandlers);
    if(!uri.startsWith("/")){
      uri = "/" + uri;
    }
    final SimpleRequestBuilder requestBuilder = SimpleRequestBuilder.create(method)
        .setUri(baseUri.resolve(uri));
    if(requestHandler !=null){
      requestHandlers.add(requestHandler);
    }
    for (RequestHandler handle : requestHandlers) {
      handle.handle(requestBuilder);
    }
    SimpleHttpRequest httpRequest = requestBuilder.build();
    CompletableFuture<SimpleHttpResponse> completableFuture = new CompletableFuture<>();
    FutureCallback<SimpleHttpResponse> callback = new FutureCallback<SimpleHttpResponse>() {
      @Override
      public void completed(SimpleHttpResponse simpleHttpResponse) {
        completableFuture.complete(simpleHttpResponse);
      }

      @Override
      public void failed(Exception e) {
        completableFuture.completeExceptionally(e);
      }

      @Override
      public void cancelled() {

      }
    };
    client.getHttpAsyncClient().execute(httpRequest, callback);
    return completableFuture;

  }

  @Override
  public <T> CompletableFuture<T> doAsyncRequest(String method, String uri, RequestHandler requestHandler, ResponseHandler<T> responseHandler) {
    final CompletableFuture<T> completableFuture = new CompletableFuture<>();
    try {
      CompletableFuture<SimpleHttpResponse> future = doAsyncRequest(method, uri, requestHandler);
      future.whenComplete((result,e)->{
        if(e==null){
          T data = null;
          try {
            data = responseHandler.handleResponse(result);
            completableFuture.complete(data);
          } catch (Exception e1) {
            LOG.warn("handleResponse error",e1);
            completableFuture.completeExceptionally(e1);
          }
        }else{
          completableFuture.completeExceptionally(e);
        }
      });
    } catch (Exception e) {
      LOG.warn("doAsyncRequest error",e);
      completableFuture.completeExceptionally(e);
    }
    return completableFuture;
  }

  @Override
  public <T> CompletableFuture<T> doAsyncRequest(String method, String uri, Map<String, Object> params, ResponseHandler<T> responseHandler) {
    return doAsyncRequest(method, uri, getParamsHandle(params), responseHandler);
  }

  @Override
  public CompletableFuture<N3Map> asyncRequest(String method, String uri, RequestHandler requestHandler) {
    return doAsyncRequest(method, uri, requestHandler, new N3MapResponseHandler());
  }

  @Override
  public CompletableFuture<N3Map> asyncRequest(String method, String uri, Map<String, Object> params) {
    return doAsyncRequest(method, uri, getParamsHandle(params), new N3MapResponseHandler());
  }

  @Override
  public CompletableFuture<N3Map> asyncRequest(String method, String uri) {
    return doAsyncRequest(method, uri, (RequestHandler)null, new N3MapResponseHandler());
  }

  @Override
  public CompletableFuture<N3Map> asyncGet(String uri, Map<String, Object> params) {
    return doAsyncRequest(HttpMethod.GET.name(), uri, getParamsHandle(params), new N3MapResponseHandler());
  }

  @Override
  public CompletableFuture<N3Map> asyncPost(String uri, Map<String, Object> params) {
    return doAsyncRequest(HttpMethod.POST.name(), uri, getParamsHandle(params), new N3MapResponseHandler());
  }

  @Override
  public CompletableFuture<N3Map> asyncPut(String uri, Map<String, Object> params) {
    return doAsyncRequest(HttpMethod.PUT.name(), uri, getParamsHandle(params), new N3MapResponseHandler());
  }

  @Override
  public CompletableFuture<N3Map> asyncDelete(String uri, Map<String, Object> params) {
    return doAsyncRequest(HttpMethod.DELETE.name(), uri, getParamsHandle(params), new N3MapResponseHandler());
  }

  @Override
  public CompletableFuture<N3Map> asyncGet(String uri, RequestHandler requestHandler) {
    return doAsyncRequest(HttpMethod.GET.name(), uri, requestHandler, new N3MapResponseHandler());
  }

  @Override
  public CompletableFuture<N3Map> asyncPost(String uri, RequestHandler requestHandler) {
    return doAsyncRequest(HttpMethod.POST.name(), uri, requestHandler, new N3MapResponseHandler());
  }

  @Override
  public CompletableFuture<N3Map> asyncPut(String uri, RequestHandler requestHandler) {
    return doAsyncRequest(HttpMethod.PUT.name(), uri, requestHandler, new N3MapResponseHandler());
  }

  @Override
  public CompletableFuture<N3Map> asyncDelete(String uri, RequestHandler requestHandler) {
    return doAsyncRequest(HttpMethod.DELETE.name(), uri, requestHandler, new N3MapResponseHandler());
  }

  @Override
  public CompletableFuture<N3Map> asyncGet(String uri) {
    return doAsyncRequest(HttpMethod.GET.name(), uri, (RequestHandler)null, new N3MapResponseHandler());
  }

  @Override
  public CompletableFuture<N3Map> asyncPost(String uri) {
    return doAsyncRequest(HttpMethod.POST.name(), uri, (RequestHandler)null, new N3MapResponseHandler());
  }

  @Override
  public CompletableFuture<N3Map> asyncPut(String uri) {
    return doAsyncRequest(HttpMethod.PUT.name(), uri, (RequestHandler)null, new N3MapResponseHandler());
  }

  @Override
  public CompletableFuture<N3Map> asyncDelete(String uri) {
    return doAsyncRequest(HttpMethod.DELETE.name(), uri, (RequestHandler)null, new N3MapResponseHandler());
  }


  @Override
  public <T> T doRequest(String method, String uri, RequestHandler requestHandler, ResponseHandler<T> responseHandler) throws IOException, ParseException, ExecutionException, InterruptedException {
    SimpleHttpResponse response = doRequest(method,uri, requestHandler);
    return responseHandler.handleResponse(response);
  }

  @Override
  public N3Map request(String method, String uri, RequestHandler requestHandler) {
    N3Map n3Map = new N3Map();
    try {
      N3Map data = doRequest(method, uri, requestHandler,new N3MapResponseHandler());
      n3Map.putAll(data);
    } catch (IOException | ParseException | ExecutionException | InterruptedException e) {
      n3Map.put(EXCEPTION,e);
      n3Map.put(E,e);
      n3Map.put(ERROR,e.getClass().getSimpleName());
      //LOG.warn(null,e);
    }
    if(checker!=null&&checker.postCheck(this, n3Map)){
      n3Map = request(method, uri, requestHandler);
    }
    return n3Map;
  }


  protected SimpleHttpResponse doRequest(String method, String uri, Map<String, Object> params) throws IOException, ExecutionException, InterruptedException {
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
  public <T> T doRequest(String method, String uri, Map<String, Object> params, ResponseHandler<T> responseHandler) throws IOException, ParseException, ExecutionException, InterruptedException {
    SimpleHttpResponse response = doRequest(method,uri, params);
    return responseHandler.handleResponse(response);
  }


  protected SimpleHttpResponse doRequest(HttpMethod method, String uri, RequestHandler requestHandler) throws IOException, ExecutionException, InterruptedException {
    return doRequest(method.name(),uri, requestHandler);
  }



  protected SimpleHttpResponse doRequest(HttpMethod method, String uri, Map<String, Object> params) throws IOException, ExecutionException, InterruptedException {
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
