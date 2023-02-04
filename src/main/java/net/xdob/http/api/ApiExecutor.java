package net.xdob.http.api;

public interface ApiExecutor {
  <V> V execute(ApiCallable<V> callable) throws Exception;
}
