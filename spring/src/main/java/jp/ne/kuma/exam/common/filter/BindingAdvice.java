package jp.ne.kuma.exam.common.filter;

import java.beans.PropertyEditorSupport;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import jp.ne.kuma.exam.common.enumerator.QuestionMode;

@ControllerAdvice
public class BindingAdvice {

  @InitBinder
  public void bind(WebDataBinder binder) {
    binder.registerCustomEditor(QuestionMode.class, new QuestionModeConverter());
  }

  public static class QuestionModeConverter extends PropertyEditorSupport {

    @Override
    public void setAsText(String value) {
      setValue(QuestionMode.asText(value));
    }
  }
}
