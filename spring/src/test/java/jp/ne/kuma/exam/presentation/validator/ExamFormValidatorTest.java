package jp.ne.kuma.exam.presentation.validator;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;

import jp.ne.kuma.exam.common.annotation.UnitTest;
import jp.ne.kuma.exam.common.util.TestUtil;
import jp.ne.kuma.exam.presentation.form.ExamForm;

@RunWith(SpringRunner.class)
@UnitTest
public class ExamFormValidatorTest {

  @InjectMocks
  private ExamFormValidator validator;
  @Mock
  private Errors errors;

  @Test
  public void ExamFormをサポートする() {
    assertThat(validator.supports(ExamForm.class)).isTrue();
  }

  @Test
  public void チェック正常系() {

    // 想定値の設定
    final ExamForm form = TestUtil.excel.loadAsPojo(ExamForm.class, this.getClass(), "ExamForm", "Data1");

    // 試験実行
    validator.validate(form, errors);

    // エラーが設定されない検証
    verify(errors, never()).rejectValue(any(String.class), any(String.class));
  }

  @Test
  public void 試験名が未設定() {

    // 想定値の設定
    final ExamForm form = TestUtil.excel.loadAsPojo(ExamForm.class, this.getClass(), "ExamForm", "Data2");

    // 試験実行
    validator.validate(form, errors);

    // エラー検証
    verify(errors, times(1)).rejectValue("examName", "exam.error.exam.nameIsEmpty");
    verifyNoMoreInteractions(errors);
  }
}
