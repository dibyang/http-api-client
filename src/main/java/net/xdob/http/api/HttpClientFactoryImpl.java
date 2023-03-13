package net.xdob.http.api;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ls.luava.common.N3Map;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author yangzj
 * @date 2021/6/16
 */
public class HttpClientFactoryImpl implements HttpClientFactory {
  static Logger LOG = LoggerFactory.getLogger(HttpClientFactory.class);

  public final static int DEFAULT_CONNECT_TIMEOUT = 5 * 1000;
  public final static int DEFAULT_SOCKET_TIMEOUT = 5 * 1000;
  public final static String DEFAULT_CHARSET = "utf-8";


  private int connTimeout = DEFAULT_CONNECT_TIMEOUT;
  private int soTimeout = DEFAULT_SOCKET_TIMEOUT;
  private TlsStrategy tlsStrategy;
  private final Map<Type,List<ResponseHandler>> responseHandlerMap = Maps.newConcurrentMap();
  private final Map<String,ParamSign> paramSignMap = Maps.newConcurrentMap();
  private boolean connManagerShared;
  private final HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom();

  public HttpClientFactoryImpl() {

    this.addResponseHandler(N3Map.class,new N3MapResponseHandler());
  }

  public int getConnTimeout() {
    return connTimeout;
  }

  public HttpClientFactory setConnTimeout(int connTimeout) {
    this.connTimeout = connTimeout;
    return this;
  }

  public int getSoTimeout() {
    return soTimeout;
  }

  public boolean isConnManagerShared() {
    return connManagerShared;
  }

  public void setConnManagerShared(boolean connManagerShared) {
    this.connManagerShared = connManagerShared;
    clientBuilder.setConnectionManagerShared(connManagerShared);
  }

  @Override
  public <T> ResponseHandler<T> getResponseHandler(Type type, String name) {
    List<ResponseHandler> responseHandlers = responseHandlerMap.get(type);
    if (responseHandlers != null) {
      if(Strings.isNullOrEmpty(name)) {
        return responseHandlers.stream().findFirst().orElse(null);
      }else{
        return responseHandlers.stream().filter(h->h.getClass().getSimpleName()
            .equals(name)).findFirst().orElse(null);
      }
    }
    return null;
  }

  @Override
  public void setParamSign(String signMethod, ParamSign paramSign) {
    paramSignMap.put(signMethod, paramSign);
  }

  @Override
  public void removeParamSign(String signMethod) {
    paramSignMap.remove(signMethod);
  }

  @Override
  public ParamSign getParamSign(String signMethod) {
    return paramSignMap.get(signMethod);
  }

  @Override
  public <T> boolean addResponseHandler(Type type, ResponseHandler<T> responseHandler) {
    if(responseHandler!=null) {
      synchronized (responseHandlerMap) {
        List<ResponseHandler> responseHandlers = responseHandlerMap.get(type);
        if (responseHandlers == null) {
          responseHandlers = Lists.newArrayList();
          responseHandlerMap.put(type, responseHandlers);
        }
        responseHandlers.add(responseHandler);
        return true;
      }
    }
    return false;
  }

  @Override
  public <T> void removeResponseHandler(Type type, ResponseHandler<T> responseHandler) {
    if(responseHandler!=null) {
      synchronized (responseHandlerMap) {
        List<ResponseHandler> responseHandlers = responseHandlerMap.get(type);
        if (responseHandlers != null) {
          responseHandlers.remove(responseHandler);
        }
      }
    }
  }


  public HttpClientFactory setSoTimeout(int soTimeout) {
    this.soTimeout = soTimeout;
    return this;
  }


  protected SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext sc = SSLContext.getInstance("TLS");

    // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
    X509TrustManager trustManager = new X509TrustManager() {
      @Override
      public void checkClientTrusted(
        java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
        String paramString) throws CertificateException {
      }

      @Override
      public void checkServerTrusted(
        java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
        String paramString) throws CertificateException {
      }

      @Override
      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
      }
    };

    sc.init(null, new TrustManager[] { trustManager }, new java.security.SecureRandom());
    return sc;
  }


  @Override
  public HttpClientFactory disableSslVerification() {
    try {
      //采用绕过验证的方式处理https请求
      SSLContext sslcontext = createIgnoreVerifySSL();

      //设置协议http和https对应的处理socket链接工厂的对象
      HostnameVerifier hostnameVerifier = new NoopHostnameVerifier();
      tlsStrategy = ClientTlsStrategyBuilder.create()
          .setSslContext(sslcontext)
          .setHostnameVerifier(hostnameVerifier).build();

    } catch (Exception e) {
      LOG.warn("",e);
    }
    return this;
  }

  @Override
  public HttpClient getHttpClient() {
    return getHttpClient(getSoTimeout(), getConnTimeout());
  }

  @Override
  public HttpClient getHttpClient(int soTimeout, int connTimeout) {

    IFaceRoutePlanner routePlanner = new IFaceRoutePlanner(DefaultSchemePortResolver.INSTANCE);
    CloseableHttpAsyncClient httpClient = getHttpClient(routePlanner);
    return new HttpClientImpl(httpClient,routePlanner, this);

  }

  @Override
  public <V> V call(HttpCallable<V> callable) throws Exception {
    try(HttpClient httpClient = getHttpClient()) {
      return callable.call(httpClient);
    }
  }

  @Override
  public ApiExecutor getApiExecutor(String baseUri, List<Header> headers, PostChecker checker, RequestHandler... handles) {
    HttpClient httpClient = getHttpClient();
    ApiClient apiClient = httpClient.getApiClient(baseUri, headers, checker, handles);
    return new ApiExecutorImpl(apiClient);
  }

  @Override
  public ApiExecutor getApiExecutor(String baseUri, List<Header> headers, RequestHandler... handles) {
    HttpClient httpClient = getHttpClient();
    ApiClient apiClient = httpClient.getApiClient(baseUri, headers, handles);
    return new ApiExecutorImpl(apiClient);
  }

  @Override
  public ApiExecutor getApiExecutor(String baseUri) {
    HttpClient httpClient = getHttpClient();
    ApiClient apiClient = httpClient.getApiClient(baseUri);
    return new ApiExecutorImpl(apiClient);
  }

  private CloseableHttpAsyncClient getHttpClient(IFaceRoutePlanner routePlanner) {
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectTimeout(Timeout.of(getConnTimeout(), TimeUnit.MILLISECONDS))
        .setConnectionRequestTimeout(Timeout.of(getSoTimeout(),TimeUnit.MILLISECONDS)).build();
    IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
        .setSoTimeout(Timeout.of(getConnTimeout(), TimeUnit.MILLISECONDS))          // 1.1
        .setSelectInterval(TimeValue.ofMilliseconds(50))    // 1.2
        .build();
    return clientBuilder.setConnectionManagerShared(connManagerShared)
        .setConnectionManager(getConnectionManager())
        .setIOReactorConfig(ioReactorConfig)
        .setRoutePlanner(routePlanner)
        .setDefaultRequestConfig(requestConfig)
        .build();
  }



  private AsyncClientConnectionManager getConnectionManager() {
    PoolingAsyncClientConnectionManager connManager = PoolingAsyncClientConnectionManagerBuilder.create()
        .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.LAX)  // 2.1
        .setTlsStrategy(tlsStrategy)
        .setMaxConnPerRoute(6).build();

    return connManager;
  }

}
