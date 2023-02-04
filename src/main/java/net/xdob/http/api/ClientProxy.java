package net.xdob.http.api;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.xdob.http.api.annotation.ExtParams;
import net.xdob.http.api.annotation.Mapping;
import net.xdob.http.api.annotation.Param;
import com.ls.luava.common.N3Map;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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



  private Map<String, Object> getRequestParams(Method method, List<ArgParam> argParams) {
    Map<String, Object> map = Maps.newHashMap();

    ExtParams ext = method.getAnnotation(ExtParams.class);
    if (ext != null) {
      for (String key : ext.value()) {
        map.put(key, client.getParams().get(key));
      }
    }

    for (ArgParam ap : argParams) {
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
            if(type instanceof ParameterizedType){
              ParameterizedType parameterizedType = (ParameterizedType) type;
              if(parameterizedType.getRawType().equals(FutureCallback.class)) {
                futureCallbackDefine.setDataType(parameterizedType.getActualTypeArguments()[0]);
              }
            }
          }
          return futureCallbackDefine;
        }
      }
    }
    return null;
  }

  String getParameterName(Parameter parameter){
    Param param = parameter.getAnnotation(Param.class);
    String name = null;
    if(param!=null){
      name = param.value();
    }
    if(Strings.isNullOrEmpty(name)&&parameter.isNamePresent()){
      name = parameter.getName();
    }
    return name;
  }

  private List<ArgParam> getArgParams(Method method, Object[] args) {
    List<ArgParam> argParams = Lists.newArrayList();
    int index = 0;
    Parameter[] parameters = method.getParameters();
    for (Parameter parameter : parameters) {
      Object arg = args[index];
      if(!(arg instanceof FutureCallback)){
        String name = getParameterName(parameter);
        if(name!=null){
          ArgParam argParam = new ArgParam();
          argParam.setArg(arg);
          argParam.setName(name);
          argParams.add(argParam);
        }
      }
      index++;
    }

    return argParams;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Mapping mapping = method.getAnnotation(Mapping.class);
    if(mapping!=null) {
      String uri = mapping.value();
      List<ArgParam> argParams = this.getArgParams(method, args);
      Map<String, Object> reqParams = this.getRequestParams(method, argParams);
      HttpMethod httpMethod = mapping.method();

      FutureCallbackDefine  futureCallback = getFutureCallback(args);
      if(futureCallback!=null){
        asyncInvoke(futureCallback, method, mapping, uri, reqParams, httpMethod);
      }else {
        Type returnType = method.getGenericReturnType();
        ParameterizedType genericReturnType = null;
        if(returnType!=null&&returnType instanceof ParameterizedType){
          genericReturnType = (ParameterizedType)returnType;
        }
        if(genericReturnType !=null
            &&Future.class.equals(genericReturnType.getRawType())){
          FutureCallbackDefine  futureCallback2 = new FutureCallbackDefine(null);
          futureCallback2.setDataType(genericReturnType.getActualTypeArguments()[0]);
          return asyncInvoke(futureCallback2, method, mapping, uri, reqParams, httpMethod);
        }else {
          return syncInvoke(method, mapping, uri, reqParams, httpMethod);
        }
      }
    }
    return null;
  }

  private Future<?> asyncInvoke(FutureCallbackDefine  futureCallback, Method method, Mapping mapping, String uri, Map<String, Object> reqParams, HttpMethod httpMethod) throws Throwable {
    //Type returnType = futureCallback.getDataType();
    Type returnType = futureCallback.getDataType();

    ResponseHandler<?> responseHandler = client.getHttpClient().getFactoryConfig().getResponseHandler(returnType, mapping.handlerName());
    FutureCallback callback = futureCallback.getCallback();
    if(responseHandler!=null){
      return client.getRestAsyncClient().doAsyncRequest(httpMethod.name(), uri, reqParams, responseHandler, callback);
    }else{
      final Future<N3Map> future = client.getRestAsyncClient().asyncRequest(httpMethod.name(), uri, reqParams, new FutureCallback<N3Map>() {
        @Override
        public void completed(N3Map map) {
          if(callback!=null) {
            try {
              Object value = wrapValue(method, mapping, returnType, map);
              callback.completed(value);
            } catch (Exception e) {
              callback.failed(e);
            }
          }
        }

        @Override
        public void failed(Exception ex) {
          if(callback!=null){
            callback.failed(ex);
          }
        }

        @Override
        public void cancelled() {
          if(callback!=null){
            callback.cancelled();
          }
        }
      });
      return new Future<Object>() {
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
          return future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
          return future.isCancelled();
        }

        @Override
        public boolean isDone() {
          return future.isDone();
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException {
          N3Map map = future.get();
          try {
            return wrapValue(method, mapping, returnType, map);
          } catch (Exception e) {
            throw new ExecutionException(e);
          }
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
          N3Map map = future.get(timeout,unit);
          try {
            return wrapValue(method, mapping, returnType, map);
          } catch (Exception e) {
            throw new ExecutionException(e);
          }
        }
      };
    }
  }

  private Object syncInvoke(Method method, Mapping mapping, String uri, Map<String, Object> reqParams, HttpMethod httpMethod) throws Throwable {
    Type returnType = method.getReturnType();
    Type genericReturnType = method.getGenericReturnType();
    if(genericReturnType!=null){
      returnType = genericReturnType;
    }
    ResponseHandler<?> responseHandler = client.getHttpClient().getFactoryConfig().getResponseHandler(returnType, mapping.handlerName());
    if(responseHandler!=null){
      return client.getRestClient().doRequest(httpMethod.name(), uri, reqParams, responseHandler);
    }else{
      final N3Map map = client.getRestClient().request(httpMethod.name(), uri, reqParams);
      return wrapValue(method, mapping, returnType, map);
    }
  }

  private Object wrapValue(Method method, Mapping mapping, Type returnType, N3Map map) throws Exception {
    Object e = map.get(ApiClient.EXCEPTION);

    if(e instanceof Throwable){
      Class<?>[] exceptionTypes = method.getExceptionTypes();
      if(exceptionTypes!=null){
        for (Class<?> exceptionType : exceptionTypes) {
          if(exceptionType.isAssignableFrom(e.getClass())){
            throw (Exception)e;
          }
        }
      }

    }

    String[] returnKey = mapping.returnKey();
    if (returnKey == null || returnKey.length == 0) {
      if (map.containsKey(N3MapResponseHandler.RETURN__)) {
        return getValue(map, returnType, N3MapResponseHandler.RETURN__);
      } else {
        return getValue(map, returnType);
      }
    } else {

      return getValue(map, returnType, returnKey);
    }
  }

  private Object getValue(N3Map map, Type returnType, String... returnKey) {
    if (returnKey != null && returnKey.length > 0) {
      if(returnType instanceof ParameterizedType){
        ParameterizedType parameterizedType = (ParameterizedType)returnType;
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
