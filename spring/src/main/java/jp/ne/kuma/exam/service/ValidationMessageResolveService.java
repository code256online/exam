package jp.ne.kuma.exam.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * バリデーションエラーを画面表示用に変換するサービス
 *
 * @author Mike
 *
 */
@Service
public class ValidationMessageResolveService {

  /** メッセージソース */
  @Autowired
  MessageSource messageSource;

  /**
   * バリデーションエラーを画面表示用に変換
   *
   * @param bindingResult
   *          バリデーション結果
   * @param locale
   *          ロケール
   * @return 画面表示用エラー情報
   */
  public Map<String, List<String>> resolve(BindingResult bindingResult, Locale locale) {

    Map<String, List<String>> ret = new HashMap<>();
    for (ObjectError error : bindingResult.getAllErrors()) {

      final String fieldname = ((FieldError) error).getField();
      final String message = messageSource.getMessage(error.getCode(), error.getArguments(), locale);
      if (ret.containsKey(fieldname)) {
        ret.get(fieldname).add(message);
      } else {
        List<String> messages = new ArrayList<>();
        messages.add(message);
        ret.put(fieldname, messages);
      }
    }

    return ret;
  }
}
