package jp.ne.kuma.exam.common.filter;

import javax.servlet.http.HttpSession;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jp.ne.kuma.exam.common.bean.QAHistory;

@ControllerAdvice
@Profile("junit")
public class MockQAHistoryAdvice {

  @ModelAttribute
  public void setDefaultHistory(HttpSession session) {

    if (session.getAttribute("scopedTarget.QAHistory") == null) {
      session.setAttribute("scopedTarget.QAHistory", new QAHistory());
    }
  }
}
