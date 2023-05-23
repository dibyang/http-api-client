package net.xdob.http.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiExecutorImpl implements ApiExecutor{
  static final Logger LOG = LoggerFactory.getLogger(ApiExecutor.class);

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

  @Override
  public <V> V executeNoThrow(ApiCallable<V> callable) {
    try {
      return execute(callable);
    } catch (Exception e) {
      LOG.warn("execute throw Exception",e);
    }
    return null;
  }
}
