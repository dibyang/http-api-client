package net.xdob.http.api;

public interface ApiExecutor {
  <V> V execute(ApiCallable<V> callable) throws Exception;
  <V> V executeNoThrow(ApiCallable<V> callable);
}
