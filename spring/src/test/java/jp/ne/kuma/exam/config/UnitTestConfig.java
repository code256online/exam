package jp.ne.kuma.exam.config;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Configuration
@Profile("junit")
@ComponentScan(includeFilters = @ComponentScan.Filter(ControllerAdvice.class))
public class UnitTestConfig {

  @Bean
  public Clock clock() {
    Clock clock = mock(Clock.class);
    doReturn(ZoneId.systemDefault()).when(clock).getZone();
    return mock(Clock.class);
  }

  @Bean
  public CsrfTokenRepository csrfTokenRepository() {
    CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    repository.setHeaderName("X-XSRF-TOKEN");
    return repository;
  }

  @Bean
  public SessionRepositoryFilter<MapSession> sessionrepositoryFilter() {

    SessionRepositoryFilter<MapSession> filter = new SessionRepositoryFilter<>(sessionRepository());
    filter.setHttpSessionIdResolver(HeaderHttpSessionIdResolver.xAuthToken());
    return filter;
  }

  @Bean
  public SessionRepository<MapSession> sessionRepository() {
    return new MapSessionRepository(new ConcurrentHashMap<>());
  }
}
