package com.ls.http.api;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ls.luava.common.N3Map;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;

/**
 * @author yangzj
 * @date 2021/6/16
 */
public class ApiClientFactoryImpl implements ApiClientFactory {
  static Logger LOG = LoggerFactory.getLogger(ApiClientFactory.class);

  public final static int DEFAULT_CONNECT_TIMEOUT = 5 * 1000;
  public final static int DEFAULT_SOCKET_TIMEOUT = 5 * 1000;
  public final static String DEFAULT_CHARSET = "utf-8";


  private int connTimeout = DEFAULT_CONNECT_TIMEOUT;
  private int soTimeout = DEFAULT_SOCKET_TIMEOUT;
  private Registry<ConnectionSocketFactory> socketFactoryRegistry;
  private final Map<Class,List<ResponseHandler>> responseHandlerMap = Maps.newConcurrentMap();


  public ApiClientFactoryImpl() {
    this.addResponseHandler(N3Map.class,new N3MapResponseHandler());
  }

  public int getConnTimeout() {
    return connTimeout;
  }

  public ApiClientFactory setConnTimeout(int connTimeout) {
    this.connTimeout = connTimeout;
    return this;
  }

  public int getSoTimeout() {
    return soTimeout;
  }


  @Override
  public <T> ResponseHandler<T> getResponseHandler(Class<T> type, String name) {
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
  public <T> boolean addResponseHandler(Class<T> type, ResponseHandler<T> responseHandler) {
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
  public <T> void removeResponseHandler(Class<T> type, ResponseHandler<T> responseHandler) {
    if(responseHandler!=null) {
      synchronized (responseHandlerMap) {
        List<ResponseHandler> responseHandlers = responseHandlerMap.get(type);
        if (responseHandlers != null) {
          responseHandlers.remove(responseHandler);
        }
      }
    }
  }


  @Override
  public void shutdown() {
    try {
      getConnectionManager().close();
    } catch (IOException e) {
      LOG.warn("",e);
    }
  }

  public ApiClientFactory setSoTimeout(int soTimeout) {
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
  public ApiClientFactory disableSslVerification() {
    try {
      //采用绕过验证的方式处理https请求
      SSLContext sslcontext = createIgnoreVerifySSL();

      //设置协议http和https对应的处理socket链接工厂的对象
      HostnameVerifier hostnameVerifier = new NoopHostnameVerifier();
      final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslcontext,hostnameVerifier);

      socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        .register("http", PlainConnectionSocketFactory.INSTANCE)
        .register("https", socketFactory)
        .build();

    } catch (Exception e) {
      LOG.warn("",e);
    }
    return this;
  }

  @Override
  public ApiClient getApiClient(String baseUri) {
    return getApiClient(baseUri,null);
  }

  @Override
  public ApiClient getApiClient(String baseUri, List<Header> headers, RequestHandler... handles) {
    return getApiClient(baseUri, headers, null, handles);
  }

  @Override
  public ApiClient getApiClient(String baseUri, List<Header> headers, PostChecker checker, RequestHandler... handles) {
    List<RequestHandler> requestHandlers = Lists.newArrayList(handles);
    if(headers!=null&&headers.size()>0){
      requestHandlers.add(builder-> headers.forEach(header->builder.addHeader(header)));
    }
    HttpClientConnectionManager connManager = getConnectionManager();
    return new ApiClientImpl(baseUri, requestHandlers, this, connManager, checker);
  }

  private HttpClientConnectionManager getConnectionManager() {
    PoolingHttpClientConnectionManager connManager = null;
    if (socketFactoryRegistry != null) {
      connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    }else{
      connManager = new PoolingHttpClientConnectionManager();
    }
    return connManager;
  }

}
