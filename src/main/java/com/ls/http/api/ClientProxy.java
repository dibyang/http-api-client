package com.ls.http.api;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ls.http.api.annotation.ExtParams;
import com.ls.http.api.annotation.Mapping;
import com.ls.http.api.annotation.Param;
import com.ls.luava.common.N3Map;
import com.ls.luava.common.Types;
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

  public ClientProxy(ApiClient client,Class<T> clazz) {
    this.client = client;
    instance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, this);
  }

  public T getInstance() {
    return instance;
  }

  private String getUri(Method method) {
    StringBuilder uri = new StringBuilder();
    Mapping ann = method.getDeclaringClass().getAnnotation(Mapping.class);
    if (ann != null) {
      uri.append(ann.value());
    }
    ann = method.getAnnotation(Mapping.class);
    if (ann != null) {
      uri.append(ann.value());
    }
    return uri.toString();
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
    String uri = this.getUri(method);
    List<ArgParm> argParams = this.getArgParms(method, args);
    Map<String, Object> reqParams = this.getRequestParams(method, argParams);
    final N3Map map = client.post(uri, reqParams);
    final Class<?> returnType = method.getReturnType();
    if(!returnType.isAssignableFrom(Map.class)){
      Mapping ann = method.getAnnotation(Mapping.class);
      String[] returnKey = ann.returnKey();
      if(returnKey==null||returnKey.length==0) {
        return Types.cast(map, returnType);
      }else{
        return map.getValue(returnType,returnKey).orElse(null);
      }
    }
    return map;
  }
}
