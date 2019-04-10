package jp.ne.kuma.exam.presentation.validator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import jp.ne.kuma.exam.persistence.AnswersDao;
import jp.ne.kuma.exam.presentation.form.AnswerForm;

/**
 * 出題データ作成編集画面入力情報バリデータ
 *
 * @author Mike
 *
 */
@Component
public class AnswerFormValidator implements Validator {

  /** 試験の答えテーブル DAO */
  @Autowired
  private AnswersDao answersDao;

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return AnswerForm.class.isAssignableFrom(clazz);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(Object target, Errors errors) {

    AnswerForm form = (AnswerForm) target;

    if (form.getExamNo() == null) {
      // 試験種別が選択されていない
      errors.rejectValue("examNo", "exam.error.answer.examNoIsEmpty");
    }

    if (form.getExamCoverage() == null) {
      // 試験範囲が選択されていない
      errors.rejectValue("examCoverage", "exam.error.answer.examCoverageIsEmpty");
    }

    if (form.getQuestionNo() == null) {
      // 問題番号が入力されていない
      errors.rejectValue("questionNo", "exam.error.answer.questionNoIsEmpty");
    }

    if (form.getChoicesCount() == null) {
      // 選択肢数が入力されていない
      errors.rejectValue("choicesCount", "exam.error.answer.choicesCountIsEmpty");
    }

    if (StringUtils.isBlank(form.getCorrectAnswers())) {
      // 正答が入力されていない
      errors.rejectValue("correctAnswers", "exam.error.answer.correctAnswersIsEmpty");
    } else {
      String[] answers = StringUtils.split(form.getCorrectAnswers(), ",");
      for (String num : answers) {
        if (num.charAt(0) < 'A' || num.charAt(0) > 'Z') {
          // 正答の入力形式が正しくない
          errors.rejectValue("questions", "exam.error.answer.correctAnswersIsInvalid");
          break;
        }
      }
    }

    // ここからは新規登録時のみ
    if (form.isInsertMode()) {

      // 同一試験種別の試験番号重複チェック
      answersDao.selectOne(form.getExamNo(), form.getQuestionNo(), null)
          .ifPresent(x -> errors.rejectValue("questionNo", "exam.error.answer.examNoIsDuplicated"));

      if (form.getFile() == null || ArrayUtils.isEmpty(form.getFile().getBytes())) {
        // 画像ファイルが設定されていない
        errors.rejectValue("file", "exam.error.answer.imageIsEmpty");
      }
    }
  }
}
