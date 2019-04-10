package jp.ne.kuma.exam.presentation.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import jp.ne.kuma.exam.presentation.form.ExamForm;

/**
 * 試験種別の作成編集画面の画面入力情報バリデータ
 *
 * @author Mike
 */
@Component
public class ExamFormValidator implements Validator {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return ExamForm.class.isAssignableFrom(clazz);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(Object target, Errors errors) {

    ExamForm form = (ExamForm) target;

    if (StringUtils.isBlank(form.getExamName())) {
      // 試験種別名が設定されていない
      errors.rejectValue("examName", "exam.error.exam.nameIsEmpty");
    }
  }
}
