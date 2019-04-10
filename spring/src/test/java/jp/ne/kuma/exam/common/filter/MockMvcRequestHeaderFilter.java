package jp.ne.kuma.exam.common.filter;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.support.WebTestUtils;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.session.MapSession;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.context.WebApplicationContext;

public class MockMvcRequestHeaderFilter implements RequestPostProcessor {

  private WebApplicationContext context;

  public MockMvcRequestHeaderFilter(WebApplicationContext context) {
    this.context = context;
  }

  @Override
  public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {

    CsrfTokenRepository csrfRepository = WebTestUtils.getCsrfTokenRepository(request);
    CsrfToken token = csrfRepository.generateToken(request);
    csrfRepository.saveToken(token, request, new MockHttpServletResponse());

    request.addHeader("X-XSRF-TOKEN", token.getToken());
    request.addHeader("X-AUTH-TOKEN", request.getSession().getId());
    request.setCookies(new Cookie("XSRF-TOKEN", token.getToken()), new Cookie("JSESSIONID", request.getSession().getId()));

    csrfRepository.loadToken(request);

    @SuppressWarnings("unchecked")
    SessionRepository<MapSession> sessionRepository = context.getBean(SessionRepository.class);
    sessionRepository.save(new MapSession(new MockHttpSessionWrapper((MockHttpSession) request.getSession())));

    return request;
  }

  public static class MockHttpSessionWrapper implements Session, Serializable {

    private static final long serialVersionUID = -4063648254434782317L;

    private MockHttpSession session;

    private MockHttpSessionWrapper(MockHttpSession session) {
      this.session = session;
    }

    @Override
    public String getId() {
      return session.getId();
    }

    @Override
    public String changeSessionId() {
      return session.changeSessionId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(String attributeName) {
      return (T) session.getAttribute(attributeName);
    }

    @Override
    public Set<String> getAttributeNames() {

      Set<String> ret = new LinkedHashSet<>();
      Enumeration<String> attrs = session.getAttributeNames();
      synchronized (attrs) {
        while(attrs.hasMoreElements()) {
          ret.add(attrs.nextElement());
        }
      }
      return Collections.unmodifiableSet(ret);
    }

    @Override
    public void setAttribute(String attributeName, Object attributeValue) {
      session.setAttribute(attributeName, attributeValue);
    }

    @Override
    public void removeAttribute(String attributeName) {
      session.removeAttribute(attributeName);
    }

    @Override
    public Instant getCreationTime() {
      return Instant.ofEpochMilli(session.getCreationTime());
    }

    @Override
    public void setLastAccessedTime(Instant lastAccessedTime) {
      session.access();
    }

    @Override
    public Instant getLastAccessedTime() {
      return Instant.ofEpochMilli(session.getLastAccessedTime());
    }

    @Override
    public void setMaxInactiveInterval(Duration interval) {
      session.setMaxInactiveInterval((int) interval.getSeconds());
    }

    @Override
    public Duration getMaxInactiveInterval() {
      return Duration.ofMinutes(30);
    }

    @Override
    public boolean isExpired() {
      return session.isInvalid();
    }
  }
}
