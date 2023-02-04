#http-api-client
1. 支持Rest请求测试
```java
    //示例代码
    public class RestTest() {
      public static void main(String[] args) {
        HttpClientFactory httpClientFactory = new HttpClientFactoryImpl();
        try (HttpClient httpClient = httpClientFactory.getHttpClient()) {
          ApiClient client = httpClient.getApiClient("https://xxxxx");
          RestAsyncClient restAsyncClient = client.getRestAsyncClient();
          RestClient restClient = client.getRestClient();
          //do something...
        } catch (Exception e) {
          LOG.warn(null, e);
        }
      }
    }
```

2. 支持接口代理测试

```java
    //示例代码
public interface AioMonitor {
  /**
   * 获取监控信息
   * @return
   */
  @Mapping("/api/store/node/monitor/cluster/info")
  ApiModel getClusterInfo();

  /**
   * 获取监控信息
   * @return
   */
  @Mapping("/api/store/node/monitor/cluster/info")
  Future<ApiModel> getAsyncClusterInfo(FutureCallback<ApiModel> callback);

  /**
   * 获取聚合监控信息
   * @return
   */
  @Mapping("/api/aio4center/monitor/info/get")
  Future<ApiModel> getAsyncClusterInfo2(FutureCallback<ApiModel> callback);

}

public class Test() {
  public static void main(String[] args) {
    HttpClientFactory httpClientFactory = new HttpClientFactoryImpl();
    try (HttpClient httpClient = httpClientFactory.getHttpClient()) {
      ApiClient client = httpClient.getApiClient("https://xxxxx");
      AioMonitor aioMonitor = client.getApiProxy(AioMonitor.class);
      //do something...
      ApiModel clusterInfo = aioMonitor.getClusterInfo();
    } catch (Exception e) {
      LOG.warn(null, e);
    }
  }
}

```

接口代理时
jdk8+ 设置 javac -parameters  方法参数名和请求参数名一致时可以省略Param注解

gradle 设置如下
```Groovy
  tasks.withType(JavaCompile) {
    options.compilerArgs << "-parameters"
  }

```



