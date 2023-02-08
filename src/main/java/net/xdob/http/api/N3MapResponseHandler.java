package net.xdob.http.api;

import com.ls.luava.common.Jsons;
import com.ls.luava.common.N3Map;
import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;


import java.io.IOException;

public class N3MapResponseHandler extends AbstractResponseHandler<N3Map> {

  public static final String APPLICATION_COMPLEX = "application/complex";
  public static final String RETURN__ = "__RETURN__";
  public static final String CONTENT_TYPE = "ContentType";

  @Override
  public N3Map handleBody(SimpleBody body) throws IOException, ParseException {
    N3Map data = new N3Map();
    ContentType contentType =body.getContentType();
    if(contentType==null){
      contentType = ContentType.APPLICATION_JSON;
    }
    data.put(CONTENT_TYPE,contentType.getMimeType());
    if(ContentType.APPLICATION_JSON.isSameMimeType(contentType)){
      String json = body.getBodyText();
      data.putAll(Jsons.i.fromJson(json,N3Map.class));
    }else if(ContentType.DEFAULT_TEXT.isSameMimeType(contentType)){
      String json = body.getBodyText();
      data.putAll(Jsons.i.fromJson(json,N3Map.class));
    }else if(ContentType.DEFAULT_BINARY.isSameMimeType(contentType)
        ||ContentType.APPLICATION_OCTET_STREAM.isSameMimeType(contentType)
        ||ContentType.APPLICATION_PDF.isSameMimeType(contentType)){
      byte[] buffer = body.getBodyBytes();
      data.put(CONTENT_TYPE,contentType.getMimeType());
      data.put(RETURN__,buffer);
    }else if(contentType.getMimeType().endsWith("json")){
      String json = body.getBodyText();
      data.putAll(Jsons.i.fromJson(json,N3Map.class));
    }else if(contentType.getMimeType().endsWith("xml")){
      String xml = body.getBodyText();
      data.put(RETURN__,xml);
    }else if(contentType.getMimeType().startsWith("image/")){
      byte[] buffer = body.getBodyBytes();
      data.put(RETURN__,buffer);
    }else if(contentType.getMimeType().startsWith("text/")){
      String text = body.getBodyText();
      data.put(RETURN__,text);
    }else{
      String text = body.getBodyText();
      data.put(RETURN__,text);
    }
    return data;
  }
}
