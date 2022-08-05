package com.ls.http.api;

import com.ls.luava.common.Jsons;
import com.ls.luava.common.N3Map;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class N3MapResponseHandler extends AbstractResponseHandler<N3Map> {

  public static final String APPLICATION_COMPLEX = "application/complex";
  public static final String RETURN__ = "__RETURN__";

  @Override
  public N3Map handleEntity(HttpEntity entity) throws IOException {
    N3Map data = new N3Map();
    ContentType contentType = ContentType.get(entity);
    if(contentType==null){
      contentType = ContentType.DEFAULT_BINARY;
    }
    if(ContentType.APPLICATION_JSON.getMimeType().equals(contentType.getMimeType())){
      String json = EntityUtils.toString(entity);
      data.putAll(Jsons.i.fromJson(json,N3Map.class));
    }else if(APPLICATION_COMPLEX.equals(contentType.getMimeType())){
      String json = EntityUtils.toString(entity);
      data.putAll(Jsons.i.fromJson(json,N3Map.class));
    }else{
      int length = (int)entity.getContentLength();
      byte[] buffer = new byte[length];
      if(length>0){
        entity.getContent().read(buffer);
      }
      data.put(RETURN__,buffer);
    }
    return data;
  }
}
