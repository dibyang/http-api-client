package com.ls.http.api;

import com.google.common.base.Strings;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.routing.RoutingSupport;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IFaceRoutePlanner implements HttpRoutePlanner {
  static final Logger LOG = LoggerFactory.getLogger(IFaceRoutePlanner.class);

  private String iface;

  private final SchemePortResolver schemePortResolver;

  public IFaceRoutePlanner(final SchemePortResolver schemePortResolver) {
    super();
    this.schemePortResolver = schemePortResolver != null ? schemePortResolver : DefaultSchemePortResolver.INSTANCE;
  }

  @Override
  public final HttpRoute determineRoute(final HttpHost host, final HttpContext context) throws HttpException {
    if (host == null) {
      throw new ProtocolException("Target host is not specified");
    }
    final HttpClientContext clientContext = HttpClientContext.adapt(context);
    final RequestConfig config = clientContext.getRequestConfig();
    HttpHost proxy = config.getProxy();
    if (proxy == null) {
      proxy = determineProxy(host, context);
    }
    HttpHost target = RoutingSupport.normalize(host, schemePortResolver);
    if (target.getPort() < 0) {
      throw new ProtocolException("Unroutable protocol scheme: " + target);
    }
    String hostName = target.getHostName();
    if(hostName.startsWith("[")&&hostName.endsWith("]")){
      hostName = hostName.substring(1,hostName.length()-1);
      if(!Strings.isNullOrEmpty(iface)){
        target = new HttpHost(target.getSchemeName(), getInetAddress(hostName,iface) ,target.getPort());
      }
    }

    final boolean secure = target.getSchemeName().equalsIgnoreCase("https");
    if (proxy == null) {
      return new HttpRoute(target, determineLocalAddress(target, context), secure);
    }
    return new HttpRoute(target, determineLocalAddress(proxy, context), proxy, secure);
  }

  private InetAddress getInetAddress(String hostName, String iface)  {
    try {
      return InetAddress.getByName(hostName + "%" + iface);
    } catch (UnknownHostException e) {
      LOG.warn("",e);
    }
    return null;
  }

  /**
   * This implementation returns null.
   *
   * @throws HttpException may be thrown if overridden
   */
  protected HttpHost determineProxy(
      final HttpHost target,
      final HttpContext context) throws HttpException {
    return null;
  }

  /**
   * This implementation returns null.
   *
   * @throws HttpException may be thrown if overridden
   */
  protected InetAddress determineLocalAddress(
      final HttpHost firstHop,
      final HttpContext context) throws HttpException {
    return null;
  }

  public String getIface() {
    return iface;
  }

  public void setIface(String iface) {
    this.iface = iface;
  }
}
