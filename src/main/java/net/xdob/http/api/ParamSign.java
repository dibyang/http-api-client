package net.xdob.http.api;


import java.util.Map;

public interface ParamSign {
  String sign(String signKey, Map<String, Object> reqParams);
}
