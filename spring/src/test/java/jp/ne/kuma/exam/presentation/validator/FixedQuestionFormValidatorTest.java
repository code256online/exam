package jp.ne.kuma.exam.presentation.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;

import jp.ne.kuma.exam.common.annotation.UnitTest;
import jp.ne.kuma.exam.common.util.TestUtil;
import jp.ne.kuma.exam.persistence.AnswersDao;
import jp.ne.kuma.exam.presentation.form.FixedQuestionForm;

@RunWith(SpringRunner.class)
@UnitTest
public class FixedQuestionFormValidatorTest {

  @InjectMocks
  private FixedQuestionFormValidator validator;
  @MockBean
  private AnswersDao answersDao;
  @Mock
  private Errors errors;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void FixedQuestionFormをサポートする() {
    assertThat(validator.supports(FixedQuestionForm.class)).isTrue();
  }

  @Test
  public void チェック正常系() {

    // 想定値の設定
    FixedQuestionForm form = TestUtil.excel.loadAsPojo(FixedQuestionForm.class, this.getClass(), "FixedQuestionForm", "Data1");

    // モック設定
    doReturn(form.getQuestions().size()).when(answersDao).countRegistered(anyList());

    // 試験実行
    validator.validate(form, errors);

    // エラーが設定されない検証
    verifyNoMoreInteractions(errors);

    // DAO呼び出し検証
    verify(answersDao, times(1)).countRegistered(form.getQuestions());
  }

  @Test
  public void 出題範囲名が設定されていない() {

    // 想定値の設定
    FixedQuestionForm form = TestUtil.excel.loadAsPojo(FixedQuestionForm.class, this.getClass(), "FixedQuestionForm", "Data2");

    // モック設定
    doReturn(form.getQuestions().size()).when(answersDao).countRegistered(anyList());

    // 試験実行
    validator.validate(form, errors);

    // エラーが設定される確認
    verify(errors, times(1)).rejectValue("name", "exam.error.fixed.nameIsEmpty");
    verifyNoMoreInteractions(errors);

    // DAO呼び出し検証
    verify(answersDao, times(1)).countRegistered(form.getQuestions());
  }

  @Test
  public void 出題番号が設定されていない() {

    // 想定値の設定
    FixedQuestionForm form = TestUtil.excel.loadAsPojo(FixedQuestionForm.class, this.getClass(), "FixedQuestionForm", "Data3");

    // 試験実行
    validator.validate(form, errors);

    // エラーが設定される確認
    verify(errors, times(1)).rejectValue("questions", "exam.error.fixed.questionsIsEmpty");
    verifyNoMoreInteractions(errors);
  }

  @Test
  public void 変な試験番号が含まれている() {

    // 想定値の設定
    FixedQuestionForm form = TestUtil.excel.loadAsPojo(FixedQuestionForm.class, this.getClass(), "FixedQuestionForm", "Data4");

    // モック設定
    doReturn(0).when(answersDao).countRegistered(anyList());

    // 試験実行
    validator.validate(form, errors);

    // エラーが設定される確認
    verify(errors, times(5)).rejectValue("questions", "exam.error.fixed.questionIsNotFound");
    verifyNoMoreInteractions(errors);

    // DAO呼び出し検証
    verify(answersDao, times(1)).countRegistered(form.getQuestions());
}
}
