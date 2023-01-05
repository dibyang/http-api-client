package com.ls.http.api;

import com.ls.luava.common.N3Map;

import java.util.List;

/**
 * @author yangzj
 * @date 2021/6/17
 */
public class ApiClientFactoryTest {
  public static void main(String[] args) {
    HttpClientFactoryImpl factory = new HttpClientFactoryImpl();
    try(HttpClient httpClient = factory.getHttpClient()){
      ApiClient client = httpClient.getApiClient("https://doob.net.cn:8443/");
      final ByPass proxy = client.getApiProxy(ByPass.class);
      final List<N3Map> list = proxy.getClients("server");
      System.out.println("list = " + list);
    }catch (Exception e){
      e.printStackTrace();
    }


  }
}
