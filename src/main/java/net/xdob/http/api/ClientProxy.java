package net.xdob.http.api;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.xdob.http.api.annotation.ExtParams;
import net.xdob.http.api.annotation.Mapping;
import net.xdob.http.api.annotation.Param;
import com.ls.luava.common.N3Map;
import net.xdob.http.api.annotation.Sign;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
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
      if(Future.class.isAssignableFrom(method.getReturnType())){
        return asyncInvoke(method, mapping, uri, reqParams, httpMethod);
      }else {
        return syncInvoke(method, mapping, uri, reqParams, httpMethod);
      }
    }
    return null;
  }

  private CompletableFuture<?> asyncInvoke(Method method, Mapping mapping, String uri, Map<String, Object> reqParams, HttpMethod httpMethod) throws Throwable {
    Type returnType = method.getGenericReturnType();
    if(returnType instanceof ParameterizedType){
      returnType = ((ParameterizedType)returnType).getActualTypeArguments()[0];
    }
    final Type dataType = returnType;
    RequestHandler requestHandler = builder->{
      Sign sign = method.getAnnotation(Sign.class);
      if(sign!=null){
        ParamSign paramSign = this.client.getHttpClient().getFactoryConfig().getParamSign(sign.signMethod());
        if(paramSign!=null){
          String signValue = paramSign.sign(sign.key(), reqParams);
          if(sign.header()){
            builder.addHeader(sign.value(),signValue);
          }else{
            reqParams.put(sign.value(),signValue);
          }
        }
      }
      RequestHandler paramsHandler = client.getParamsHandle(reqParams);
      paramsHandler.handle(builder);
    };
    ResponseHandler<?> responseHandler = client.getHttpClient().getFactoryConfig().getResponseHandler(dataType, mapping.handlerName());

    if(responseHandler!=null){
      return client.getRestAsyncClient().doAsyncRequest(httpMethod.name(), uri, requestHandler, responseHandler);
    }else{
      CompletableFuture<Object> completableFuture = new CompletableFuture<>();
      final CompletableFuture<N3Map> future = client.getRestAsyncClient().asyncRequest(httpMethod.name(), uri, requestHandler);
      future.whenComplete((r,e)->{
        if(e==null){
          try {
            Object value = wrapValue(method, mapping, dataType, r);
            completableFuture.complete(value);
          } catch (Exception ex) {
            completableFuture.completeExceptionally(ex);
          }
        }else{
          completableFuture.completeExceptionally(e);
        }
      });
      return completableFuture;
    }
  }

  private Object syncInvoke(Method method, Mapping mapping, String uri, Map<String, Object> reqParams, HttpMethod httpMethod) throws Throwable {
    Type returnType = method.getReturnType();
    Type genericReturnType = method.getGenericReturnType();
    if(genericReturnType!=null){
      returnType = genericReturnType;
    }
    RequestHandler requestHandler = builder->{
      Sign sign = method.getAnnotation(Sign.class);
      if(sign!=null){
        ParamSign paramSign = this.client.getHttpClient().getFactoryConfig().getParamSign(sign.signMethod());
        if(paramSign!=null){
          String signValue = paramSign.sign(sign.key(), reqParams);
          if(sign.header()){
            builder.addHeader(sign.value(),signValue);
          }else{
            reqParams.put(sign.value(),signValue);
          }
        }
      }
      RequestHandler paramsHandler = client.getParamsHandle(reqParams);
      paramsHandler.handle(builder);
    };
    ResponseHandler<?> responseHandler = client.getHttpClient().getFactoryConfig().getResponseHandler(returnType, mapping.handlerName());
    if(responseHandler!=null){
      return client.getRestClient().doRequest(httpMethod.name(), uri, requestHandler, responseHandler);
    }else{
      final N3Map map = client.getRestClient().request(httpMethod.name(), uri, requestHandler);
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
