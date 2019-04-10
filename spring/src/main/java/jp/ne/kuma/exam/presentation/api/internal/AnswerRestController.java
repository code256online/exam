package jp.ne.kuma.exam.presentation.api.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.ne.kuma.exam.persistence.dto.Answer;
import jp.ne.kuma.exam.persistence.dto.AnswerPageDto;
import jp.ne.kuma.exam.presentation.form.AnswerForm;
import jp.ne.kuma.exam.presentation.validator.AnswerFormValidator;
import jp.ne.kuma.exam.service.EditQuestionService;
import jp.ne.kuma.exam.service.InitializeService;
import jp.ne.kuma.exam.service.ValidationMessageResolveService;

/**
 * 出題データ関連 REST コントローラ
 *
 * @author Mike
 */
@RestController
@RequestMapping("/api/internal/answer")
public class AnswerRestController {

  /** 初期化サービス */
  @Autowired
  private InitializeService initializeService;
  /** 出題データメンテナンスサービス */
  @Autowired
  private EditQuestionService service;
  /** 出題データ編集登録バリデータ */
  @Autowired
  private AnswerFormValidator answerValidator;
  /** 入力エラーメッセージ変換サービス */
  @Autowired
  private ValidationMessageResolveService messageResolver;

  /**
   * バリデータを登録
   *
   * @param binder
   *          バインダー
   */
  @InitBinder("answerForm")
  public void bindAnswerFormValidator(WebDataBinder binder) {
    binder.addValidators(answerValidator);
  }

  /**
   * 出題データメンテナンス画面での出題データ一覧ページを取得
   *
   * @param examNo
   *          試験番号
   * @param page
   *          ページ
   * @return 出題データ一覧の 1 ページ分のデータ
   */
  @GetMapping
  public Page<AnswerPageDto> getSelectPage(@RequestParam Integer examNo, @RequestParam Integer page) {

    Pageable pageable = PageRequest.of(page, 10, Sort.by(Direction.ASC, "question_no"));
    return initializeService.getAnswerPage(examNo, pageable);
  }

  /**
   * 試験番号と問題番号に紐付く出題データを取得（廃止フラグ設定済みを含む）
   *
   * @param examNo
   *          試験番号
   * @param questionNo
   *          問題番号
   * @return 出題データ
   */
  @GetMapping("/{examNo}/{questionNo}")
  public Answer getExactAnswer(@PathVariable("examNo") Integer examNo, @PathVariable("questionNo") Integer questionNo) {
    return initializeService.getExactAnswer(examNo, questionNo).get();
  }

  /**
   * 出題データ編集更新画面のバリデーションを行う
   *
   * @param answerForm
   *          入力データ
   * @param bindingResult
   *          バリデーション結果
   * @param locale
   *          ロケール
   * @return 入力エラー情報
   */
  @PostMapping("/validate")
  public Map<String, List<String>> validateAnswer(@Validated @RequestBody AnswerForm answerForm,
      BindingResult bindingResult, Locale locale) {

    return messageResolver.resolve(bindingResult, locale);
  }

  /**
   * 引数の試験番号が設定されている出題データ（廃止フラグ設定を含む）のうち、最大の問題番号を取得
   *
   * @param examNo
   *          試験番号
   * @return 最大の問題番号
   */
  @GetMapping("/maxQuestionNo/{examNo}")
  public int maxQuestionNo(@PathVariable Integer examNo) {
    return service.getMaxQuestionNo(examNo);
  }

  /**
   * 出題データ新規登録
   *
   * @param form
   *          入力データ
   * @param bindingResult
   *          バリデーション結果
   */
  @PutMapping
  public void createAnswer(@Validated @RequestBody AnswerForm form, BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      throw new ValidationException();
    }
    service.insertAnswer(form);
  }

  /**
   * 出題データ更新登録
   *
   * @param form
   *          入力データ
   * @param bindingResult
   *          バリデーション結果
   */
  @PostMapping
  public void editAnswer(@Validated @RequestBody AnswerForm form, BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      throw new ValidationException();
    }
    service.updateAnswer(form);
  }

  /**
   * 出題データ削除
   *
   * @param examNo
   *          試験番号
   * @param questionNo
   *          問題番号
   */
  @DeleteMapping("/{examNo}/{questionNo}")
  public void deleteAnswer(@PathVariable("examNo") Integer examNo, @PathVariable("questionNo") Integer questionNo) {
    service.deleteAnswer(examNo, questionNo);
  }
}
