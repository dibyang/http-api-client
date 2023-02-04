package net.xdob.http.api;

import net.xdob.http.api.annotation.Mapping;
import net.xdob.http.api.annotation.Param;
import com.ls.luava.common.N3Map;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @author yangzj
 * @date 2021/6/17
 */
public interface ByPass {
  @Mapping(value = "/api/p2p/mgr/client/get",returnKey = "data")
  List<N3Map> getClients(@Param("type") String type);

  @Mapping(value = "/api/p2p/mgr/client/get",returnKey = "data")
  void getAsyncClients(@Param("type") String type, FutureCallback<List<N3Map>> callback);

  @Mapping(value = "/api/p2p/mgr/client/get",returnKey = "data")
  Future<List<N3Map>> getAsyncClients2(@Param("type") String type);
}
