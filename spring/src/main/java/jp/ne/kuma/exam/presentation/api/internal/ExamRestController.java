package jp.ne.kuma.exam.presentation.api.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

import jp.ne.kuma.exam.persistence.dto.Exam;
import jp.ne.kuma.exam.presentation.form.ExamForm;
import jp.ne.kuma.exam.presentation.validator.ExamFormValidator;
import jp.ne.kuma.exam.service.EditQuestionService;
import jp.ne.kuma.exam.service.InitializeService;
import jp.ne.kuma.exam.service.ValidationMessageResolveService;

/**
 * 試験情報関連 REST コントローラ
 *
 * @author Mike
 */
@RestController
@RequestMapping("/api/internal/exam")
public class ExamRestController {

  /** 初期化サービス */
  @Autowired
  private InitializeService initializeService;
  /** 出題データメンテナンスサービス */
  @Autowired
  private EditQuestionService service;
  /** 試験情報作成編集画面入力情報バリデータ */
  @Autowired
  private ExamFormValidator examValidator;
  /** 入力エラーメッセージ変換サービス */
  @Autowired
  private ValidationMessageResolveService messageResolver;

  /**
   * バリデータを登録
   *
   * @param binder
   *          バインダー
   */
  @InitBinder("examForm")
  public void bindExamValidator(WebDataBinder binder) {
    binder.addValidators(examValidator);
  }

  /**
   * 全ての試験情報を取得
   *
   * @return 試験情報リスト
   */
  @GetMapping
  public List<Exam> getAll() {
    return initializeService.getAllExams();
  }

  /**
   * 試験種別データを取得
   *
   * @param examNo
   *          試験種別
   * @return 試験種別データ
   */
  @GetMapping("/{examNo}")
  public Exam getExam(@PathVariable("examNo") Integer examNo) {
    return initializeService.getExactExam(examNo).get();
  }

  /**
   * 試験種別の登録更新画面の入力情報バリデーション
   *
   * @param examForm
   *          画面入力情報
   * @param bindingResult
   *          バリデーション結果
   * @param locale
   *          ロケール
   * @return エラーメッセージ
   */
  @PostMapping("/validate")
  public Map<String, List<String>> validateExam(@Validated @RequestBody ExamForm examForm, BindingResult bindingResult,
      Locale locale) {

    return messageResolver.resolve(bindingResult, locale);
  }

  /**
   * 試験種別データの新規登録
   *
   * @param examForm
   *          画面入力情報
   * @param bindingResult
   *          バリデーション結果
   */
  @PutMapping
  public void createExam(@Validated @RequestBody ExamForm examForm, BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      throw new IllegalStateException();
    }
    service.insertExam(examForm);
  }

  /**
   * 試験種別データの更新登録
   *
   * @param examForm
   *          画面入力情報
   * @param bindingResult
   *          バリデーション結果
   */
  @PostMapping
  public void editExam(@Validated @RequestBody ExamForm examForm, BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      throw new IllegalStateException();
    }
    service.updateExam(examForm);
  }

  /**
   * 試験種別データ削除
   *
   * @param examNo
   *          試験種別
   */
  @DeleteMapping("/{examNo}")
  public void deleteExam(@PathVariable("examNo") Integer examNo) {
    service.deleteExam(examNo);
  }
}
