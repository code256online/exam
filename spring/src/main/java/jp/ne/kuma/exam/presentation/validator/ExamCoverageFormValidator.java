package jp.ne.kuma.exam.presentation.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import jp.ne.kuma.exam.presentation.form.ExamCoverageForm;

/**
 * 試験範囲データの作成編集画面の画面入力バリデータ
 *
 * @author Mike
 *
 */
@Component
public class ExamCoverageFormValidator implements Validator {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return ExamCoverageForm.class.isAssignableFrom(clazz);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(Object target, Errors errors) {

    ExamCoverageForm form = (ExamCoverageForm) target;

    if (form.getExamNo() == null || form.getExamNo() <= 0) {
      // 試験種別が選択されていない
      errors.rejectValue("examNo", "exam.error.coverage.examNoIsEmpty");
    }

    if (StringUtils.isBlank(form.getName())) {
      // 試験範囲名が設定されていない
      errors.rejectValue("name", "exam.error.coverage.nameIsEmpty");
    }
  }
}
