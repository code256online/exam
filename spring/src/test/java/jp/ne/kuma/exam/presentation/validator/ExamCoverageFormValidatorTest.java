package jp.ne.kuma.exam.presentation.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;

import jp.ne.kuma.exam.common.annotation.UnitTest;
import jp.ne.kuma.exam.common.util.TestUtil;
import jp.ne.kuma.exam.presentation.form.ExamCoverageForm;

@RunWith(SpringRunner.class)
@UnitTest
public class ExamCoverageFormValidatorTest {

  @InjectMocks
  private ExamCoverageFormValidator validator;
  @Mock
  private Errors errors;

  @Test
  public void ExamCoverageFormをサポートする() {
    assertThat(validator.supports(ExamCoverageForm.class)).isTrue();
  }

  @Test
  public void チェック正常系() {

    // 想定値設定
    final ExamCoverageForm form = TestUtil.excel.loadAsPojo(ExamCoverageForm.class, this.getClass(), "ExamCoverageForm", "Data1");

    // 試験実行
    validator.validate(form, errors);

    // エラーが設定されない検証
    verify(errors, never()).rejectValue(any(String.class), any(String.class));
  }

  @Test
  public void 試験番号が設定されていない() {

    // 想定値設定
    final ExamCoverageForm form = TestUtil.excel.loadAsPojo(ExamCoverageForm.class, this.getClass(), "ExamCoverageForm", "Data2");

    // 試験実行
    validator.validate(form, errors);

    // エラー検証
    verify(errors, times(1)).rejectValue("examNo", "exam.error.coverage.examNoIsEmpty");
    verifyNoMoreInteractions(errors);
  }

  @Test
  public void 試験番号が不正な値() {

    // 想定値設定
    final ExamCoverageForm form = TestUtil.excel.loadAsPojo(ExamCoverageForm.class, this.getClass(), "ExamCoverageForm", "Data3");

    // 試験実行
    validator.validate(form, errors);

    // エラー検証
    verify(errors, times(1)).rejectValue("examNo", "exam.error.coverage.examNoIsEmpty");
    verifyNoMoreInteractions(errors);
  }

  @Test
  public void 試験範囲名が設定されていない() {

    // 想定値設定
    final ExamCoverageForm form = TestUtil.excel.loadAsPojo(ExamCoverageForm.class, this.getClass(), "ExamCoverageForm", "Data4");

    // 試験実行
    validator.validate(form, errors);

    // エラー検証
    verify(errors, times(1)).rejectValue("name", "exam.error.coverage.nameIsEmpty");
    verifyNoMoreInteractions(errors);
  }
}
