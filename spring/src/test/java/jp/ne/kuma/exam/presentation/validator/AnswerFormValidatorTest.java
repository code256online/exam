package jp.ne.kuma.exam.presentation.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.Optional;

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
import jp.ne.kuma.exam.persistence.dto.Answer;
import jp.ne.kuma.exam.presentation.form.AnswerForm;

@RunWith(SpringRunner.class)
@UnitTest
public class AnswerFormValidatorTest {

  @InjectMocks
  private AnswerFormValidator validator;
  @MockBean
  private AnswersDao answersDao;
  @Mock
  private Errors errors;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void AnswerFormをサポートする() {
    assertThat(validator.supports(AnswerForm.class)).isTrue();
  }

  @Test
  public void チェック正常系() throws IOException {

    // 想定値設定
    AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data1");
    form.getFile().setBytes(TestUtil.getDataFileAsBytes(this.getClass(), "E001Q001.jpg"));
    final Optional<Answer> duplicated = Optional.empty();

    // モック設定
    doReturn(duplicated).when(answersDao).selectOne(anyInt(), anyInt(), any());

    // 試験実行
    validator.validate(form, errors);

    // エラーが設定されない検証
    verify(errors, never()).rejectValue(any(String.class), any(String.class));
    verify(answersDao, times(1)).selectOne(form.getExamNo(), form.getQuestionNo(), null);
  }

  @Test
  public void 試験番号が設定されていない() {

    // 想定値設定
    final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data2");

    // 試験実行
    validator.validate(form, errors);

    // エラーの確認
    verify(errors, times(1)).rejectValue("examNo", "exam.error.answer.examNoIsEmpty");
    verifyNoMoreInteractions(errors);
  }

  @Test
  public void 試験範囲が設定されていない() {

    // 想定値設定
    final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data3");

    // 試験実行
    validator.validate(form, errors);

    // エラーの確認
    verify(errors, times(1)).rejectValue("examCoverage", "exam.error.answer.examCoverageIsEmpty");
    verifyNoMoreInteractions(errors);
  }

  @Test
  public void 問題番号が設定されていない() {

    // 想定値設定
    final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data4");

    // 試験実行
    validator.validate(form, errors);

    // エラーの確認
    verify(errors, times(1)).rejectValue("questionNo", "exam.error.answer.questionNoIsEmpty");
    verifyNoMoreInteractions(errors);
  }

  @Test
  public void 選択肢数が設定されていない() {

    // 想定値設定
    final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data5");

    // 試験実行
    validator.validate(form, errors);

    // エラーの確認
    verify(errors, times(1)).rejectValue("choicesCount", "exam.error.answer.choicesCountIsEmpty");
    verifyNoMoreInteractions(errors);
  }

  @Test
  public void 正答が設定されていない() {

    // 想定値設定
    final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data6");

    // 試験実行
    validator.validate(form, errors);

    // エラーの確認
    verify(errors, times(1)).rejectValue("correctAnswers", "exam.error.answer.correctAnswersIsEmpty");
    verifyNoMoreInteractions(errors);
  }

  @Test
  public void 正答が半角アルファベットのカンマ区切り形式ではない1() {

    // 想定値設定
    final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data7");

    // 試験実行
    validator.validate(form, errors);

    // エラーの確認
    verify(errors, times(1)).rejectValue("questions", "exam.error.answer.correctAnswersIsInvalid");
    verifyNoMoreInteractions(errors);
  }

  @Test
  public void 正答が半角アルファベットのカンマ区切り形式ではない2() {

    // 想定値設定
    final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data8");

    // 試験実行
    validator.validate(form, errors);

    // エラーの確認
    verify(errors, times(1)).rejectValue("questions", "exam.error.answer.correctAnswersIsInvalid");
    verifyNoMoreInteractions(errors);
  }

  @Test
  public void 新規作成モードのとき試験番号と問題番号でユニークにならない() throws IOException {

    // 想定値設定
    AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data1");
    form.getFile().setBytes(TestUtil.getDataFileAsBytes(this.getClass(), "E001Q001.jpg"));
    final Answer answer = TestUtil.excel.loadAsPojo(Answer.class, this.getClass(), "Answer", "Data1");
    final Optional<Answer> duplicated = Optional.ofNullable(answer);

    // モック設定
    doReturn(duplicated).when(answersDao).selectOne(anyInt(), anyInt(), any());

    // 試験実行
    validator.validate(form, errors);

    // エラーの確認
    verify(errors, times(1)).rejectValue("questionNo", "exam.error.answer.examNoIsDuplicated");
    verifyNoMoreInteractions(errors);
    verify(answersDao, times(1)).selectOne(form.getExamNo(), form.getQuestionNo(), null);
  }

  @Test
  public void 新規作成モードのときアップロードファイルが設定されていない1() throws IOException {

    // 想定値設定
    AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data9");
    final Optional<Answer> duplicated = Optional.empty();

    // モック設定
    doReturn(duplicated).when(answersDao).selectOne(anyInt(), anyInt(), any());

    // 試験実行
    validator.validate(form, errors);

    // エラーの確認
    verify(errors, times(1)).rejectValue("file", "exam.error.answer.imageIsEmpty");
    verifyNoMoreInteractions(errors);
    verify(answersDao, times(1)).selectOne(form.getExamNo(), form.getQuestionNo(), null);
  }

  @Test
  public void 新規作成モードのときアップロードファイルが設定されていない2() throws IOException {

    // 想定値設定
    AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data1");
    final Optional<Answer> duplicated = Optional.empty();

    // モック設定
    doReturn(duplicated).when(answersDao).selectOne(anyInt(), anyInt(), any());

    // 試験実行
    validator.validate(form, errors);

    // エラーの確認
    verify(errors, times(1)).rejectValue("file", "exam.error.answer.imageIsEmpty");
    verifyNoMoreInteractions(errors);
    verify(answersDao, times(1)).selectOne(form.getExamNo(), form.getQuestionNo(), null);
  }
}
