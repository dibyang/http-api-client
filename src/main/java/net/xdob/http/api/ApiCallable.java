package net.xdob.http.api;

@FunctionalInterface
public interface ApiCallable<V> {
  /**
   * Computes a result, or throws an exception if unable to do so.
   *
   * @return computed result
   * @throws Exception if unable to compute a result
   */
  V call(ApiClient apiClient) throws Exception;
}
