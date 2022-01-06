package com.ls.http.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ls.http.api.annotation.ExtParams;
import com.ls.http.api.annotation.Mapping;
import com.ls.http.api.annotation.Param;
import com.ls.luava.common.N3Map;
import com.ls.luava.common.Types;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yangzj
 * @date 2021/6/17
 */
public class ClientProxy <T> implements InvocationHandler {
  static Logger LOG = LoggerFactory.getLogger(ClientProxy.class);

  private final ApiClient client;
  private final T instance;

  public ClientProxy(ApiClient client, Class<T> clazz) {
    this.client = client;
    instance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, this);
  }

  public T getInstance() {
    return instance;
  }



  private Map<String, Object> getRequestParams(Method method, List<ArgParm> argParams) {
    Map<String, Object> map = Maps.newHashMap();

    ExtParams ext = method.getAnnotation(ExtParams.class);
    if (ext != null) {
      for (String key : ext.value()) {
        map.put(key, client.getParams().get(key));
      }
    }

    for (ArgParm ap : argParams) {
      if (ap.hasName()) {
        Object arg = ap.getArg();
        if (arg instanceof String) {
          arg = paramDecode((String) arg);
        }
        map.put(ap.getName(), arg);
      }
    }

    return map;
  }

  private String paramDecode(String arg) {
    //将NCR编码转中文
    Pattern pattern = Pattern.compile("&#(\\d{5});");
    Matcher matcher = pattern.matcher(arg);
    while (matcher.find()) {
      arg = arg.replace(matcher.group(), String.valueOf((char)Integer.parseInt(matcher.group(1))));
    }
    return arg;
  }

  private List<ArgParm> getArgParms(Method method, Object[] args) {
    List<ArgParm> argParams = Lists.newArrayList();
    int index = 0;
    for (Annotation[] anns : method.getParameterAnnotations()) {
      ArgParm argParam = new ArgParm();
      argParam.setArg(args[index]);
      argParams.add(argParam);
      index++;
      if (anns.length > 0) {
        argParam.setAnnotations(Lists.newArrayList(anns));
        for (Annotation ann : anns) {
          if (ann instanceof Param) {
            argParam.setName(((Param) ann).value());
            break;
          }
        }
      }
    }
    return argParams;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Mapping mapping = method.getAnnotation(Mapping.class);
    if(mapping!=null) {
      String uri = mapping.value();
      List<ArgParm> argParams = this.getArgParms(method, args);
      Map<String, Object> reqParams = this.getRequestParams(method, argParams);
      HttpMethod httpMethod = mapping.method();
      final Class<?> returnType = method.getReturnType();

      if(HttpEntity.class.isAssignableFrom(returnType)){
        CloseableHttpResponse response = client.getRestClient().doRequest(httpMethod, uri, reqParams);
        return response.getEntity();
      }else if(HttpResponse.class.isAssignableFrom(returnType)){
        CloseableHttpResponse response = client.getRestClient().doRequest(httpMethod, uri, reqParams);
        return response;
      }else{
        final N3Map map = client.getRestClient().request(httpMethod, uri, reqParams);
        Object e = map.get(ApiClient.EXCEPTION);
        if(e instanceof Throwable){
          throw (Throwable)e;
        }
        String[] returnKey = mapping.returnKey();
        if (returnKey == null || returnKey.length == 0) {
          if (map.containsKey(N3MapResponseHandler.RETURN__)) {
            return map.getValue(returnType, N3MapResponseHandler.RETURN__).orElse(null);
          } else {
            return map.wrap(returnType);
          }
        } else {
          return map.getValue(returnType, returnKey).orElse(null);
        }
      }
    }
    return null;
  }
}
