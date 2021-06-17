package com.ls.http.api;

import com.google.common.collect.Lists;
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
