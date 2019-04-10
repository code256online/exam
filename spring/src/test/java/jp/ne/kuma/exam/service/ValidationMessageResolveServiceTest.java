package jp.ne.kuma.exam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import jp.ne.kuma.exam.common.annotation.UnitTest;
import jp.ne.kuma.exam.presentation.form.AnswerForm;

@RunWith(SpringRunner.class)
@UnitTest
public class ValidationMessageResolveServiceTest {

  @InjectMocks
  private ValidationMessageResolveService service;
  @MockBean
  private MessageSource messageSource;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void エラーメッセージ変換() {

    // 想定値設定
    final String fieldName = "examNo";
    final String code1 = "exam.error.answer.examNoIsEmpty";
    final String message1 = "試験種別を指定してください。";
    final String code2 = "exam.error.answer.examCoverageIsEmpty";
    final String message2 = "試験範囲を指定してください。";
    final Locale locale = Locale.getDefault();
    BindingResult bindingResult = new BeanPropertyBindingResult(new AnswerForm(), "answerForm");
    bindingResult.rejectValue(fieldName, code1);
    bindingResult.rejectValue(fieldName, code2);
    Map<String, List<String>> expected = new HashMap<>();
    expected.put(fieldName, Arrays.asList(message1, message2));

    // モック設定
    doReturn(message1).doReturn(message2).when(messageSource).getMessage(any(String.class), any(), any(Locale.class));

    // 試験実行
    Map<String, List<String>> actual = service.resolve(bindingResult, locale);

    // 検証
    assertThat(actual).isEqualTo(expected);
    verify(messageSource, times(1)).getMessage(code1, null, locale);
    verify(messageSource, times(1)).getMessage(code2, null, locale);
  }
}
