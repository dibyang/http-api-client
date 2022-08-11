package com.ls.http.api;

import com.ls.luava.common.Jsons;
import com.ls.luava.common.N3Map;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;


import java.io.IOException;

public class N3MapResponseHandler extends AbstractResponseHandler<N3Map> {

  public static final String APPLICATION_COMPLEX = "application/complex";
  public static final String RETURN__ = "__RETURN__";

  @Override
  public N3Map handleEntity(HttpEntity entity) throws IOException, ParseException {
    N3Map data = new N3Map();
    ContentType contentType = ContentType.parse(entity.getContentType());
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
