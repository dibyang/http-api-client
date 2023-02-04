package net.xdob.http.api;

import com.ls.luava.common.N3Map;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.util.List;
import java.util.concurrent.Future;

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
      proxy.getAsyncClients("server", new FutureCallback<List<N3Map>>() {
        @Override
        public void completed(List<N3Map> result) {
          System.out.println("result = " + result);
        }

        @Override
        public void failed(Exception ex) {
          ex.printStackTrace();
        }

        @Override
        public void cancelled() {

        }
      });
      Future<List<N3Map>> server = proxy.getAsyncClients2("server");
      List<N3Map> n3Maps = server.get();
      System.out.println("n3Maps = " + n3Maps);
    }catch (Exception e){
      e.printStackTrace();
    }


  }
}
