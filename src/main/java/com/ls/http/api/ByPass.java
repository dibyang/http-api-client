package com.ls.http.api;

import com.ls.http.api.annotation.Mapping;
import com.ls.http.api.annotation.Param;
import com.ls.luava.common.N3Map;

import java.util.List;

/**
 * @author yangzj
 * @date 2021/6/17
 */
public interface ByPass {
  @Mapping(value = "/api/p2p/mgr/client/get",returnKey = "data")
  List<N3Map> getClients(@Param("type") String type);
}
