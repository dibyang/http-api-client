package net.xdob.http.api;

@FunctionalInterface
public interface HttpRunnable {
  void run(HttpClient httpClient) throws Exception;
}
