package jp.ne.kuma.exam.presentation.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.LocalDateTime;
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
import jp.ne.kuma.exam.persistence.ExamCoveragesDao;
import jp.ne.kuma.exam.persistence.FixedQuestionsDao;
import jp.ne.kuma.exam.persistence.dto.ExamCoverage;
import jp.ne.kuma.exam.persistence.dto.FixedQuestion;
import jp.ne.kuma.exam.presentation.form.InitForm;

@RunWith(SpringRunner.class)
@UnitTest
public class InitFormValidatorTest {

    @InjectMocks
    private InitFormValidator validator;
    @MockBean
    private ExamCoveragesDao examCoveragesDao;
    @MockBean
    private FixedQuestionsDao fixedQuestionsDao;
    @Mock
    private Errors errors;

    @Before
    public void setup() {
      MockitoAnnotations.initMocks(this);
    }

    @Test
    public void InitFormをサポートする() {
      assertThat(validator.supports(InitForm.class)).isTrue();
    }

    @Test
    public void チェック正常系() {

      // 想定値の設定
      final InitForm form = TestUtil.excel.loadAsPojo(InitForm.class, this.getClass(), "InitForm", "Data1");
      Optional<ExamCoverage> coverage = Optional.ofNullable(TestUtil.excel.loadAsPojo(ExamCoverage.class, this.getClass(), "Coverage", "Data1"));

      // モックの設定
      doNothing().when(errors).rejectValue(any(String.class), any(String.class));
      doReturn(coverage).when(examCoveragesDao).selectExactExamCoverage(anyInt(), anyInt());

      // 試験実行
      validator.validate(form, errors);

      // エラーが設定されない確認
      verify(errors, never()).rejectValue(any(String.class), any(String.class));
    }

    @Test
    public void 試験範囲IDの異常系() {

      // 想定値の設定
      final InitForm form = TestUtil.excel.loadAsPojo(InitForm.class, this.getClass(), "InitForm", "Data2");
      Optional<ExamCoverage> coverage = Optional.empty();

      // モックの設定
      doNothing().when(errors).rejectValue(any(String.class), any(String.class));
      doReturn(coverage).when(examCoveragesDao).selectExactExamCoverage(anyInt(), anyInt());

      // 試験実行
      validator.validate(form, errors);

      // エラーが設定される確認
      verify(errors, times(1)).rejectValue("examCoverage", "exam.error.init.examCoverageIsNotFound");
      verifyNoMoreInteractions(errors);
    }

    @Test
    public void 出題数の異常系1_未入力() {

      // 想定値の設定
      final InitForm form = TestUtil.excel.loadAsPojo(InitForm.class, this.getClass(), "InitForm", "Data3");

      // モックの設定
      doNothing().when(errors).rejectValue(any(String.class), any(String.class));

      // 試験実行
      validator.validate(form, errors);

      // エラーが設定される確認
      verify(errors, times(1)).rejectValue("questionCount", "exam.error.init.invalidQuestionCount");
      verifyNoMoreInteractions(errors);
    }

    @Test
    public void 出題数の異常系2_入力値0以下() {

      // 想定値の設定
      final InitForm form = TestUtil.excel.loadAsPojo(InitForm.class, this.getClass(), "InitForm", "Data4");

      // モックの設定
      doNothing().when(errors).rejectValue(any(String.class), any(String.class));

      // 試験実行
      validator.validate(form, errors);

      // エラーが設定される確認
      verify(errors, times(1)).rejectValue("questionCount", "exam.error.init.invalidQuestionCount");
      verifyNoMoreInteractions(errors);
    }

    @Test
    public void 固定出題の正常系() {

      // 想定値の設定
      final InitForm form = TestUtil.excel.loadAsPojo(InitForm.class, this.getClass(), "InitForm", "Data5");
      Optional<FixedQuestion> fixedQuestion = Optional.ofNullable(TestUtil.excel.loadAsPojo(FixedQuestion.class, this.getClass(), "FixedQuestion", "Data1"));

      // モック設定
      doReturn(fixedQuestion).when(fixedQuestionsDao).selectOne(anyInt(), any());

      // 試験実行
      validator.validate(form, errors);

      // エラーが設定されない確認
      verify(errors, never()).rejectValue(any(String.class), any(String.class));

      // DAO呼び出し確認
      verify(fixedQuestionsDao, times(1)).selectOne(form.getFixedQuestionsId(), null);
    }

    @Test
    public void 固定出題IDが設定されていない() {

      // 想定値の設定
      final InitForm form = TestUtil.excel.loadAsPojo(InitForm.class, this.getClass(), "InitForm", "Data6");

      // 試験実行
      validator.validate(form, errors);

      // エラーが設定される確認
      verify(errors, times(1)).rejectValue("fixedQuestionsId", "exam.error.init.fixedIdIsNull");
      verifyNoMoreInteractions(errors);
    }

    @Test
    public void 存在しない固定出題IDが指定されている() {

      // 想定値の設定
      final InitForm form = TestUtil.excel.loadAsPojo(InitForm.class, this.getClass(), "InitForm", "Data5");
      Optional<FixedQuestion> fixedQuestion = Optional.empty();

      // モック設定
      doReturn(fixedQuestion).when(fixedQuestionsDao).selectOne(anyInt(), any(LocalDateTime.class));

      // 試験実行
      validator.validate(form, errors);

      // エラーが設定される確認
      verify(errors, times(1)).rejectValue("fixedQuestionsId", "exam.error.init.fixedIdIsNull");
      verifyNoMoreInteractions(errors);

      // DAO呼び出し確認
      verify(fixedQuestionsDao, times(1)).selectOne(form.getFixedQuestionsId(), null);
    }
}
