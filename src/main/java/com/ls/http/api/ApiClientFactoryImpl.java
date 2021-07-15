package com.ls.http.api;

import com.google.common.collect.Lists;
import org.apache.http.Header;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author yangzj
 * @date 2021/6/16
 */
public class ApiClientFactoryImpl implements ApiClientFactory {
  static Logger LOG = LoggerFactory.getLogger(ApiClientFactory.class);

  public final static int DEFAULT_CONNECT_TIMEOUT = 5 * 1000;
  public final static int DEFAULT_SOCKET_TIMEOUT = 5 * 1000;
  public final static String DEFAULT_CHARSET = "utf-8";
  public final static SocketConfig DEFAULT_SOCKET_CONFIG = SocketConfig.custom()
    .setSoKeepAlive(false)
    .setSoLinger(1)
    .setSoReuseAddress(true)
    .setSoTimeout(DEFAULT_SOCKET_TIMEOUT)
    .setTcpNoDelay(true)
    .build();
  private PoolingHttpClientConnectionManager connManager = null;

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

  private  CloseableHttpClient getHttpClient() {
    return HttpClients.custom()
      .setConnectionManager(connManager)
      .setConnectionTimeToLive(DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
      .setDefaultSocketConfig(DEFAULT_SOCKET_CONFIG)
      .build();
  }

  @Override
  public ApiClientFactory disableSslVerification() {
    try {
      //采用绕过验证的方式处理https请求
      SSLContext sslcontext = createIgnoreVerifySSL();

      //设置协议http和https对应的处理socket链接工厂的对象
      HostnameVerifier hostnameVerifier = new X509HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
        public void verify(String arg0, SSLSocket arg1) throws IOException {
        }
        public void verify(String arg0, X509Certificate arg1) throws SSLException {
        }
        public void verify(String arg0, String[] arg1, String[] arg2) throws SSLException {
        }
      };
      final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslcontext,hostnameVerifier);

      Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        .register("http", PlainConnectionSocketFactory.INSTANCE)
        .register("https", socketFactory)
        .build();
      connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    } catch (Exception e) {
      connManager = null;
      LOG.warn("",e);
    }
    return this;
  }

  @Override
  public ApiClient getApiClient(String baseUri) {
    return getApiClient(baseUri,null);
  }

  @Override
  public ApiClient getApiClient(String baseUri, List<Header> headers, RequestHandle... handles) {
    List<RequestHandle> requestHandles = Lists.newArrayList();
    if(headers!=null&&headers.size()>0){
      requestHandles.add(builder-> headers.forEach(header->builder.addHeader(header)));
    }
    return new ApiClientImpl(baseUri,requestHandles,DEFAULT_CONNECT_TIMEOUT,DEFAULT_SOCKET_TIMEOUT,getHttpClient());
  }

}
