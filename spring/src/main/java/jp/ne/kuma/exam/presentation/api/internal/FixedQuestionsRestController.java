package jp.ne.kuma.exam.presentation.api.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.ne.kuma.exam.presentation.form.FixedQuestionForm;
import jp.ne.kuma.exam.presentation.validator.FixedQuestionFormValidator;
import jp.ne.kuma.exam.service.EditQuestionService;
import jp.ne.kuma.exam.service.InitializeService;
import jp.ne.kuma.exam.service.ValidationMessageResolveService;

/**
 * 固定出題データ関連 REST コントローラ
 *
 * @author Mike
 */
@RestController
@RequestMapping("/api/internal/fixed")
public class FixedQuestionsRestController {

  /** 初期化サービス */
  @Autowired
  private InitializeService initService;
  /** 出題データメンテナンスサービス */
  @Autowired
  private EditQuestionService service;
  /** 固定出題データ作成編集画面入力情報バリデータ */
  @Autowired
  private FixedQuestionFormValidator fixedQuestionValidator;
  /** 画面入力エラー情報変換サービス */
  @Autowired
  private ValidationMessageResolveService messageResolver;

  /**
   * バリデータを登録
   *
   * @param binder
   *          バインダー
   */
  @InitBinder("fixedQuestionForm")
  public void bindFixedQuestionValidator(WebDataBinder binder) {
    binder.addValidators(fixedQuestionValidator);
  }

  /**
   * 全ての固定出題データを取得
   *
   * @return 固定出題データリスト
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public List<FixedQuestionForm> getFixedQuestions() {
    return initService.getFixedQuestions();
  }

  /**
   * ID で一意に定まる固定出題データを取得
   *
   * @param id
   *          固定出題 ID
   * @return 固定出題データ
   */
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public FixedQuestionForm getFixedQuestion(@PathVariable("id") Integer id) {
    return initService.getExactFixedQuestion(id).get();
  }

  /**
   * 固定出題データの作成編集画面の画面入力情報のバリデーション
   *
   * @param fixedQuestionForm
   *          画面入力情報
   * @param bindingResult
   *          バリデーション結果
   * @param locale
   *          ロケール
   * @return エラーメッセージ
   */
  @PostMapping("/validate")
  public Map<String, List<String>> validateFixedQuestion(@Validated @RequestBody FixedQuestionForm fixedQuestionForm,
      BindingResult bindingResult, Locale locale) {

    return messageResolver.resolve(bindingResult, locale);
  }

  /**
   * 固定出題データの新規作成
   *
   * @param fixedQuestionForm
   *          画面入力情報
   * @param bindingResult
   *          バリデーション結果
   */
  @PutMapping
  public int createFixedQuestion(@Validated @RequestBody FixedQuestionForm fixedQuestionForm,
      BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      throw new IllegalStateException();
    }
    return service.insertFixedQuestion(fixedQuestionForm);
  }

  /**
   * 固定出題データの更新登録
   *
   * @param fixedQuestionForm
   *          画面入力情報
   * @param bindingResult
   *          バリデーション結果
   */
  @PostMapping
  public void editFixedQuestion(@Validated @RequestBody FixedQuestionForm fixedQuestionForm,
      BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      throw new IllegalStateException();
    }
    service.updateFixedQuestion(fixedQuestionForm);
  }

  /**
   * 固定出題データ削除
   *
   * @param id
   *          固定出題 ID
   */
  @DeleteMapping("/{id}")
  public void deleteFixedQuestion(@PathVariable("id") Integer id) {
    service.deleteFixedQuestion(id);
  }
}
