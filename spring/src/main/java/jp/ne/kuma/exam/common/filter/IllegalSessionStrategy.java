package jp.ne.kuma.exam.common.filter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.session.InvalidSessionStrategy;

public class IllegalSessionStrategy implements InvalidSessionStrategy {

  @Override
  public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

    if (StringUtils.contains(request.getRequestURI(), "/api/")) {
      response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid Session Detected.");
    } else {
      response.sendRedirect("/exam");
    }
  }
}
