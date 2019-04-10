package jp.ne.kuma.exam.presentation.validator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import jp.ne.kuma.exam.persistence.AnswersDao;
import jp.ne.kuma.exam.presentation.form.FixedQuestionForm;
import jp.ne.kuma.exam.service.dto.QuestionData;

/**
 * 固定出題の作成編集画面の画面入力情報バリデータ
 *
 * @author Mike
 */
@Component
public class FixedQuestionFormValidator implements Validator {

  @Autowired
  private AnswersDao answersDao;

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return FixedQuestionForm.class.isAssignableFrom(clazz);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(Object target, Errors errors) {

    FixedQuestionForm form = (FixedQuestionForm) target;

    if (StringUtils.isBlank(form.getName())) {
      // 固定出題名が設定されていない
      errors.rejectValue("name", "exam.error.fixed.nameIsEmpty");
    }

    if (CollectionUtils.isEmpty(form.getQuestions())) {
      // 出題する問題が設定されていない
      errors.rejectValue("questions", "exam.error.fixed.questionsIsEmpty");
    } else {
      for (QuestionData data : form.getQuestions()) {
        if ((data.getExamNo() == null || data.getExamNo() < 1)
            || (data.getQuestionNo() == null || data.getQuestionNo() < 1)) {
          // なんか入力がおかしい。ここに来るのはフロントのバグ。
          errors.rejectValue("questions", "exam.error.fixed.questionIsNotFound");
        }
      }
      int count = answersDao.countRegistered(form.getQuestions());
      if (count != form.getQuestions().size()) {
        // 試験番号が未登録のものがある。
        errors.rejectValue("questions", "exam.error.fixed.questionIsNotFound");
      }
    }
  }
}
