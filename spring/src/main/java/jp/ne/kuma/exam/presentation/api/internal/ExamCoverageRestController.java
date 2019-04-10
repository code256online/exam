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

import jp.ne.kuma.exam.persistence.dto.ExamCoverage;
import jp.ne.kuma.exam.presentation.form.ExamCoverageForm;
import jp.ne.kuma.exam.presentation.validator.ExamCoverageFormValidator;
import jp.ne.kuma.exam.service.EditQuestionService;
import jp.ne.kuma.exam.service.InitializeService;
import jp.ne.kuma.exam.service.ValidationMessageResolveService;

/**
 * 試験範囲情報関連 REST コントローラ
 *
 * @author Mike
 */
@RestController
@RequestMapping("/api/internal/coverage")
public class ExamCoverageRestController {

  /** 初期化サービス */
  @Autowired
  private InitializeService initializeService;
  /** 出題データメンテナンスサービス */
  @Autowired
  private EditQuestionService service;
  /** 試験範囲入力情報バリデータ */
  @Autowired
  private ExamCoverageFormValidator coverageValidator;
  /** 入力エラーメッセージ変換サービス */
  @Autowired
  private ValidationMessageResolveService messageResolver;

  /**
   * バリデータを登録
   *
   * @param binder
   *          バインダー
   */
  @InitBinder("examCoverageForm")
  public void bindExamCoverageValidator(WebDataBinder binder) {
    binder.addValidators(coverageValidator);
  }

  /**
   * 試験番号に紐づくすべての試験範囲情報を取得
   *
   * @param examNo
   *          試験番号
   * @return 試験範囲情報リスト
   */
  @GetMapping(value = "/{examNo}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public List<ExamCoverage> get(@PathVariable Integer examNo) {
    return initializeService.getCoverages(examNo, false);
  }

  /**
   * 試験番号に紐づく、廃止フラグ設定済みを含む、すべての試験範囲情報を取得
   *
   * @param examNo
   *          試験番号
   * @return 試験範囲情報リスト
   */
  @GetMapping(value = "includeDeleted/{examNo}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public List<ExamCoverage> getIncludeDeleted(@PathVariable Integer examNo) {
    return initializeService.getCoverages(examNo, true);
  }

  /**
   * 試験種別と試験範囲 ID で一意に定まる試験範囲データを取得（廃止フラグ設定済みを含む）
   *
   * @param examNo
   *          試験種別
   * @param id
   *          試験範囲 ID
   * @return 試験範囲情報
   */
  @GetMapping(value = "/{examNo}/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ExamCoverage getExactCoverage(@PathVariable("examNo") Integer examNo, @PathVariable("id") Integer id) {
    return initializeService.getExactExamCoverage(examNo, id).get();
  }

  /**
   * 試験範囲データ編集・更新画面の入力バリデーション
   *
   * @param examCoverageForm
   *          画面入力情報
   * @param bindingResult
   *          バリデーション結果
   * @param locale
   *          ロケール
   * @return エラーメッセージ
   */
  @PostMapping("/validate")
  public Map<String, List<String>> validateExamCoverage(@Validated @RequestBody ExamCoverageForm examCoverageForm,
      BindingResult bindingResult, Locale locale) {

    return messageResolver.resolve(bindingResult, locale);
  }

  /**
   * 試験範囲データ新規登録
   *
   * @param examCoverageForm
   *          画面入力情報
   * @param bindingResult
   *          バリデーション結果
   */
  @PutMapping
  public void createExamCoverage(@Validated @RequestBody ExamCoverageForm examCoverageForm,
      BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      throw new IllegalStateException();
    }
    service.insertExamCoverage(examCoverageForm);
  }

  /**
   * 試験範囲データ更新登録
   *
   * @param examCoverageForm
   *          画面入力情報
   * @param bindingResult
   *          バリデーション結果
   */
  @PostMapping
  public void editExamCoverage(@Validated @RequestBody ExamCoverageForm examCoverageForm, BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      throw new IllegalStateException();
    }
    service.updateExamCoverage(examCoverageForm);
  }

  /**
   * 試験範囲データ削除
   *
   * @param examNo
   *          試験種別
   * @param id
   *          試験範囲 ID
   */
  @DeleteMapping("/{examNo}/{id}")
  public void deleteExamCoverage(@PathVariable("examNo") Integer examNo, @PathVariable("id") Integer id) {
    service.deleteExamCoverage(examNo, id);
  }
}
