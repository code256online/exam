package jp.ne.kuma.exam.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!junit")
public class TomcatCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

  @Override
  public void customize(TomcatServletWebServerFactory factory) {

    Connector connector = new Connector("AJP/1.3");
    connector.setUseBodyEncodingForURI(true);
    connector.setAttribute("maxThreads", 100);
    connector.setPort(8009);
    connector.setRedirectPort(8443);
    connector.setURIEncoding("UTF-8");
    factory.addAdditionalTomcatConnectors(connector);
  }
}
