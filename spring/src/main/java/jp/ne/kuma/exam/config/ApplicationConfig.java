package jp.ne.kuma.exam.config;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Random;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import net.rakugakibox.util.YamlResourceBundle;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

  @Value("${spring.messages.basename}")
  private String basename;
  @Value("${spring.messages.encoding}")
  private String encoding;

  @Override
  public void addCorsMappings(CorsRegistry registry) {

    registry.addMapping("/**")
        .allowedOrigins("http://localhost:4200")
        .allowedMethods("*")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
  }

  @Bean
  public Random random() {
    return new SecureRandom();
  }

  @Bean("messageSource")
  public MessageSource messageSource() {

    YamlMessageSource messageSource = new YamlMessageSource();
    messageSource.setBasename(basename);
    messageSource.setDefaultEncoding(encoding);
    messageSource.setAlwaysUseMessageFormat(true);
    messageSource.setUseCodeAsDefaultMessage(true);
    messageSource.setFallbackToSystemLocale(true);

    return messageSource;
  }

  @Bean
  public LocalValidatorFactoryBean validator() {

    LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
    bean.setValidationMessageSource(messageSource());
    return bean;
  }

  @Override
  public Validator getValidator() {
    return validator();
  }

  @Bean
  public CookieSerializer cookieSerializer(@Value("${exam.cookie.secure}") boolean secure,
      @Value("${server.servlet.context-path}") String contextPath) {

    DefaultCookieSerializer serializer = new DefaultCookieSerializer();
    serializer.setCookieName("JSESSIONID");
    serializer.setCookiePath(contextPath);
    serializer.setDomainNamePattern("^.+\\.([a-z-]+\\.[a-z]+)$");
    serializer.setUseSecureCookie(secure);
    return serializer;
  }

  @Bean
  @Profile("!junit")
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}

class YamlMessageSource extends ResourceBundleMessageSource {
  @Override
  protected ResourceBundle doGetBundle(String basename, Locale locale) throws MissingResourceException {
    return ResourceBundle.getBundle(basename, locale, YamlResourceBundle.Control.INSTANCE);
  }
}
