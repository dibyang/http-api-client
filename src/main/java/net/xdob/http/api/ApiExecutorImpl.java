package net.xdob.http.api;

public class ApiExecutorImpl implements ApiExecutor{
  private final ApiClient apiClient;

  public ApiExecutorImpl(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  @Override
  public <V> V execute(ApiCallable<V> callable) throws Exception {
    try (HttpClient httpClient = apiClient.getHttpClient()){
      return callable.call(apiClient);
    }
  }
}
