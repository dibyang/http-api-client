package com.ls.http.api;

import com.google.common.collect.Lists;
import com.ls.luava.common.N3Map;
import org.apache.http.Header;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author yangzj
 * @date 2021/6/16
 */
public class ApiClientFactoryImpl implements ApiClientFactory {
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

  private  CloseableHttpClient getHttpClient() {
    return HttpClients.custom()
      .setConnectionTimeToLive(DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
      .setDefaultSocketConfig(DEFAULT_SOCKET_CONFIG)
      .build();
  }

  @Override
  public ApiClient getRestClient(String baseUri) {
    return getRestClient(baseUri,null);
  }

  @Override
  public ApiClient getRestClient(String baseUri, List<Header> headers, RequestHandle... handles) {
    List<RequestHandle> requestHandles = Lists.newLinkedList();
    if(headers!=null&&headers.size()>0){
      requestHandles.add(builder-> headers.forEach(header->builder.addHeader(header)));
    }
    return new ApiClientImpl(baseUri,requestHandles,DEFAULT_CONNECT_TIMEOUT,DEFAULT_SOCKET_TIMEOUT,getHttpClient());
  }

  public static void main(String[] args) {
    ApiClientFactoryImpl factory = new ApiClientFactoryImpl();
    final ApiClient client = factory.getRestClient("https://doob.net.cn:8443/");
    final N3Map post = client.request("POST", "/api/p2p/mgr/client/get",h-> h.addParameter("type","server"));
    System.out.println("post = " + post);

  }
}
