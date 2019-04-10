package jp.ne.kuma.exam.presentation.validator;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import jp.ne.kuma.exam.common.enumerator.QuestionMode;
import jp.ne.kuma.exam.persistence.ExamCoveragesDao;
import jp.ne.kuma.exam.persistence.FixedQuestionsDao;
import jp.ne.kuma.exam.persistence.dto.ExamCoverage;
import jp.ne.kuma.exam.persistence.dto.FixedQuestion;
import jp.ne.kuma.exam.presentation.form.InitForm;

/**
 * 試験設定画面の画面入力情報バリデータ
 *
 * @author Mike
 */
@Component
public class InitFormValidator implements Validator {

  /** 試験範囲テーブル DAO */
  @Autowired
  private ExamCoveragesDao examCoveragesDao;
  /** 固定試験テーブル DAO */
  @Autowired
  private FixedQuestionsDao fixedQuestionsDao;

  /**
   * {@inheritDoc}n
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return InitForm.class.isAssignableFrom(clazz);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(Object target, Errors errors) {

    InitForm form = (InitForm) target;

    if (form.getQuestionMode() != QuestionMode.FIXED) {
      // 以下は固定出題モードではない場合のみ
      if (form.getExamCoverage() != -1) {
        Optional<ExamCoverage> coverage = examCoveragesDao
            .selectExactExamCoverage(form.getExamNo(), form.getExamCoverage());
        if (!coverage.isPresent()) {
          // 試験範囲が -1（全ての範囲）ではなく、試験範囲テーブルにそんな試験範囲 ID が存在しない
          errors.rejectValue("examCoverage", "exam.error.init.examCoverageIsNotFound");
        }
      }

      if (form.getQuestionCount() == null || form.getQuestionCount() < 1) {
        // 出題数が設定されていないか、0 以下が設定されている
        errors.rejectValue("questionCount", "exam.error.init.invalidQuestionCount");
      }
    } else {
      // 以下は固定出題モードの場合のみ
      if (form.getFixedQuestionsId() == null) {
        // 固定出題が選択されていない
        errors.rejectValue("fixedQuestionsId", "exam.error.init.fixedIdIsNull");
      } else {
        Optional<FixedQuestion> fixedQuestion = fixedQuestionsDao.selectOne(form.getFixedQuestionsId(), null);
        if (!fixedQuestion.isPresent()) {
          // 選択されているが、固定出題テーブルにそんな固定出題データがない
          errors.rejectValue("fixedQuestionsId", "exam.error.init.fixedIdIsNull");
        }
      }
    }
  }
}
