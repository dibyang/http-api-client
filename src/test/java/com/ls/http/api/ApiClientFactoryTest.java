package com.ls.http.api;

import com.ls.luava.common.N3Map;

import java.io.IOException;
import java.util.List;

/**
 * @author yangzj
 * @date 2021/6/17
 */
public class ApiClientFactoryTest {
  public static void main(String[] args) {
    ApiClientFactoryImpl factory = new ApiClientFactoryImpl();
    try(ApiClient client = factory.getRestClient("https://doob.net.cn:8443/")){
      final ByPass proxy = client.getProxy(ByPass.class);
      final List<N3Map> list = proxy.getClients("server");
      System.out.println("list = " + list);
    }catch (IOException e){
      e.printStackTrace();
    }


  }
}
