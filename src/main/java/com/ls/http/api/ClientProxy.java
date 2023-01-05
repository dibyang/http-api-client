package com.ls.http.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ls.http.api.annotation.ExtParams;
import com.ls.http.api.annotation.Mapping;
import com.ls.http.api.annotation.Param;
import com.ls.luava.common.N3Map;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collection;
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

  private FutureCallbackDefine getFutureCallback(Object[] args){
    if(args!=null){
      for (Object arg : args) {
        if(arg instanceof FutureCallback){
          FutureCallbackDefine futureCallbackDefine = new FutureCallbackDefine((FutureCallback)arg);
          Type[] types = arg.getClass().getGenericInterfaces();
          for (Type type : types) {
            if(type.equals(FutureCallback.class)){
              futureCallbackDefine.setDataType(((ParameterizedType)type).getActualTypeArguments()[0]);
            }
          }
          return futureCallbackDefine;
        }
      }
    }
    return null;
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

      FutureCallbackDefine  futureCallback = getFutureCallback(args);
      if(futureCallback!=null){
        asyncInvoke(futureCallback, method, mapping, uri, reqParams, httpMethod);
      }else {
        return syncInvoke(method, mapping, uri, reqParams, httpMethod);
      }
    }
    return null;
  }

  private void asyncInvoke(FutureCallbackDefine  futureCallback, Method method, Mapping mapping, String uri, Map<String, Object> reqParams, HttpMethod httpMethod) throws Throwable {
    Type returnType = futureCallback.getDataType();
    Type genericReturnType = null;
    if(returnType instanceof ParameterizedType){
      genericReturnType = ((ParameterizedType)returnType).getActualTypeArguments()[0];
    }
    ResponseHandler<?> responseHandler = client.getHttpClient().getFactoryConfig().getResponseHandler(returnType, mapping.handlerName());
    if(responseHandler!=null){
      client.getRestAsyncClient().doAsyncRequest(httpMethod.name(), uri, reqParams, responseHandler, futureCallback.getCallback());
    }else{
      Type finalGenericReturnType = genericReturnType;
      client.getRestAsyncClient().asyncRequest(httpMethod.name(), uri, reqParams, new FutureCallback<N3Map>() {
        @Override
        public void completed(N3Map map) {
          Object e = map.get(ApiClient.EXCEPTION);

          if(e instanceof Throwable){
            Class<?>[] exceptionTypes = method.getExceptionTypes();
            if(exceptionTypes!=null){
              for (Class<?> exceptionType : exceptionTypes) {
                if(exceptionType.isAssignableFrom(e.getClass())){
                  futureCallback.getCallback().failed((Exception) e);
                }
              }
            }
          }

          String[] returnKey = mapping.returnKey();
          if (returnKey == null || returnKey.length == 0) {
            if (map.containsKey(N3MapResponseHandler.RETURN__)) {
              Object value = getValue(map, returnType, finalGenericReturnType, N3MapResponseHandler.RETURN__);
              futureCallback.getCallback().completed(value);
            } else {
              Object value = getValue(map, returnType, finalGenericReturnType);
              futureCallback.getCallback().completed(value);
            }
          } else {

            Object value = getValue(map, returnType, finalGenericReturnType, returnKey);
            futureCallback.getCallback().completed(value);
          }
        }

        @Override
        public void failed(Exception ex) {
          futureCallback.getCallback().failed(ex);
        }

        @Override
        public void cancelled() {
          futureCallback.getCallback().cancelled();
        }
      });

    }
  }

  private Object syncInvoke(Method method, Mapping mapping, String uri, Map<String, Object> reqParams, HttpMethod httpMethod) throws Throwable {
    Type returnType = method.getReturnType();
    Type genericReturnType = method.getGenericReturnType();
    ResponseHandler<?> responseHandler = client.getHttpClient().getFactoryConfig().getResponseHandler(returnType, mapping.handlerName());
    if(responseHandler!=null){
      return client.getRestClient().doRequest(httpMethod.name(), uri, reqParams, responseHandler);
    }else{
      final N3Map map = client.getRestClient().request(httpMethod.name(), uri, reqParams);
      Object e = map.get(ApiClient.EXCEPTION);

      if(e instanceof Throwable){
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        if(exceptionTypes!=null){
          for (Class<?> exceptionType : exceptionTypes) {
            if(exceptionType.isAssignableFrom(e.getClass())){
              throw (Throwable)e;
            }
          }
        }

      }

      String[] returnKey = mapping.returnKey();
      if (returnKey == null || returnKey.length == 0) {
        if (map.containsKey(N3MapResponseHandler.RETURN__)) {
          return getValue(map, returnType, genericReturnType, N3MapResponseHandler.RETURN__);
        } else {
          return getValue(map, returnType, genericReturnType);
        }
      } else {

        return getValue(map, returnType, genericReturnType, returnKey);
      }
    }
  }

  private Object getValue(N3Map map, Type returnType, Type genericReturnType, String... returnKey) {
    if (returnKey != null && returnKey.length > 0) {
      if(genericReturnType instanceof ParameterizedType){
        ParameterizedType parameterizedType = (ParameterizedType)genericReturnType;
        if(Collection.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())){
          List<?> list = map.getValues((Class<?>) parameterizedType.getActualTypeArguments()[0], returnKey);
          return list;
        }
        return map;
      }else {
        return map.getValue((Class)returnType, returnKey).orElse(null);
      }
    } else {
      return map.wrap((Class)returnType);
    }
  }
}
