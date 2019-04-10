package jp.ne.kuma.exam.presentation.api.internal;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.ne.kuma.exam.common.bean.QAHistory;
import jp.ne.kuma.exam.common.bean.QAHistory.QuestionPageInfo;
import jp.ne.kuma.exam.common.bean.UserInfo;
import jp.ne.kuma.exam.common.enumerator.AnswerStates;
import jp.ne.kuma.exam.common.enumerator.QuestionMode;
import jp.ne.kuma.exam.presentation.form.InitForm;
import jp.ne.kuma.exam.presentation.validator.InitFormValidator;
import jp.ne.kuma.exam.service.HistoryService;
import jp.ne.kuma.exam.service.InitializeService;
import jp.ne.kuma.exam.service.QuestionService;
import jp.ne.kuma.exam.service.ValidationMessageResolveService;
import jp.ne.kuma.exam.service.dto.HistoryItem;
import jp.ne.kuma.exam.service.dto.Question;

/**
 * 出題関連 REST コントローラ
 *
 * @author Mike
 */
@RestController
@RequestMapping("/api/internal/question")
public class QuestionRestController {

  /** デフォルトソート */
  private static final Sort DEFAULT_SORT = Sort.by(Direction.ASC, "question_no");
  /** ページサイズ */
  private static final int PAGE_SIZE = 1;

  /** 出題関連サービス */
  @Autowired
  private QuestionService questionService;
  /** 初期化サービス */
  @Autowired
  private InitializeService initializeService;
  /** 出題初期化画面入力情報バリデータ */
  @Autowired
  private InitFormValidator initValidator;
  /** 履歴関連サービス */
  @Autowired
  private HistoryService historyService;
  /** このセッションの回答履歴 */
  @Autowired
  private QAHistory history;
  /** バリデーションエラーメッセージ変換サービス */
  @Autowired
  private ValidationMessageResolveService messageResolver;
  /** 時刻オブジェクト */
  @Autowired
  private Clock clock;
  /** ログインユーザー情報 */
  @Autowired
  private UserInfo userInfo;

  /**
   * バリデータの登録
   *
   * @param binder
   *          データバインダ
   */
  @InitBinder("initForm")
  public void initFormBinder(WebDataBinder binder) {
    binder.addValidators(initValidator);
  }

  /**
   * 回答と出題
   *
   * @param page
   *          次に出すページ
   * @param c
   *          複数選択の選択結果
   * @param r
   *          単一選択の選択結果
   * @return 問題ページ情報
   */
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Page<Question> question(@RequestParam Integer page, @RequestParam(required = false) String[] c,
      @RequestParam(required = false) String r) {

    // -1 ページがリクエストされたら最終ページがリクエストされたことにする
    if (page == -1) {
      page = history.getLastQuestion().getTotalPages() - 1;
    }

    // 答え合わせする
    determineAnswer(c, r);

    Pageable pageable = PageRequest.of(history.getAnswerStates().get(page).getQuestionNo(), PAGE_SIZE, DEFAULT_SORT);
    Pageable nextPage = PageRequest.of(page, PAGE_SIZE, DEFAULT_SORT);

    // これが次に出す問題
    Page<Question> question = questionService.getPage(history, pageable, nextPage);
    history.setLastQuestion(question);
    return question;
  }

  /**
   * セッションに初期化済みで完了していない出題履歴があるかどうか。
   *
   * @return あれば true
   */
  @GetMapping(value = "resumeCheck", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public boolean resumeCheck() {
    return history.getLastQuestion() != null;
  }

  /**
   * このセッションの出題履歴リセット。
   */
  @GetMapping("reset")
  public void reset() {
    history.reset();
  }

  /**
   * 初期化時のバリデーション
   *
   * @param initForm
   *          初期化情報
   * @param bindingResult
   *          バリデーション結果
   * @param locale
   *          ロケール
   * @return 画面項目ごとのエラーメッセージ
   */
  @PostMapping(value = "initValidation", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Map<String, List<String>> initValidation(@Validated @RequestBody InitForm initForm,
      BindingResult bindingResult, Locale locale) {

    return messageResolver.resolve(bindingResult, locale);
  }

  /**
   * 出題画面初期化
   *
   * @param initForm
   *          初期化情報
   * @param bindingResult
   *          バリデーション結果
   * @return 最初の問題
   */
  @PostMapping(value = "init", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Page<Question> initialize(@Validated @RequestBody InitForm initForm, BindingResult bindingResult) {

    // すでに初期化済みの情報がセッションにある場合、最後に出題した問題のページを返す。
    if (history.getLastQuestion() != null) {
      return history.getLastQuestion();
    }

    if (bindingResult.hasErrors()) {
      throw new IllegalStateException();
    }

    history.setStartDatetime(LocalDateTime.now(clock));
    history.setExamineeId(userInfo.getId());
    history.setExamNo(initForm.getExamNo());
    history.setQuestionMode(initForm.getQuestionMode());

    if (initForm.getQuestionMode() == QuestionMode.FIXED) {

      // 固定出題モード固有の初期化
      history.setFixedQuestionsId(initForm.getFixedQuestionsId());
      history.setAnswerStates(initializeService.getFixedQuestionNumbers(initForm.getFixedQuestionsId()).stream()
          .map(x -> {
            QuestionPageInfo info = new QuestionPageInfo();
            info.setExamNo(x.getExamNo());
            info.setQuestionNo(x.getQuestionNo());
            info.setState(AnswerStates.NOT_ANSWERED);
            return info;
          }).collect(Collectors.toList()));

    } else {

      // 通常モード固有の初期化
      history.setExamCoverage(initForm.getExamCoverage());

      // 存在する問題数よりも入力された問題数の方が多かったら、全問出す
      Integer count = questionService.getCount(initForm.getExamNo(), initForm.getExamCoverage());
      int questionCount = count <= initForm.getQuestionCount() ? count : initForm.getQuestionCount();
      history.setAnswerStates(questionService.generateUniqueRandomNumbers(count, questionCount).stream()
          .map(x -> {
            QuestionPageInfo info = new QuestionPageInfo();
            info.setExamNo(initForm.getExamNo());
            info.setQuestionNo(x);
            info.setState(AnswerStates.NOT_ANSWERED);
            return info;
          }).collect(Collectors.toList()));
    }

    Pageable pageable = PageRequest.of(history.getAnswerStates().get(0).getQuestionNo(), PAGE_SIZE, DEFAULT_SORT);
    Pageable nextPage = PageRequest.of(0, PAGE_SIZE, DEFAULT_SORT);

    // これが最初に出す問題
    Page<Question> question = questionService.getPage(history, pageable, nextPage);

    history.setLastQuestion(question);
    history.setQuestionCount((int) question.getTotalElements());

    return question;
  }

  /**
   * 回答終了して履歴書き込み
   *
   * @param c
   *          最終問題の単一選択
   * @param r
   *          最終問題の複数選択
   */
  @GetMapping("/finish")
  public void finish(@RequestParam(required = false) String[] c, @RequestParam(required = false) String r) {

    determineAnswer(c, r);
    historyService.update(history);
  }

  /**
   * 結果出力
   *
   * @return 結果ページ
   */
  @GetMapping(value = "/finishPage", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Page<HistoryItem> finishPage() {

    Page<HistoryItem> ret = historyService.getFinishPage(history.getExamineeId(), PageRequest.of(0, 10),
        history.getQuestionMode());
    // セッションの出題履歴をクリア
    history.reset();

    return ret;
  }

  /**
   * 答え合わせ
   *
   * @param c
   *          複数選択の選択結果
   * @param r
   *          単一選択の選択結果
   */
  private void determineAnswer(String[] c, String r) {

    Question lastQuestion = history.getLastQuestion().getContent().get(0);
    QuestionPageInfo lastState = history.getAnswerStates().stream()
        .filter(x -> lastQuestion.getExamNo() == x.getExamNo()
            && lastQuestion.getQuestionNo() == x.getQuestionNo())
        .findAny().get();
    String[] choices = null;

    if (StringUtils.isNotEmpty(r)) {
      // 単一選択の場合
      choices = new String[] { r };
    } else if (c != null) {
      // 複数選択の場合
      choices = Stream.of(c)
          .filter(x -> StringUtils.isNotEmpty(x))
          .toArray(String[]::new);
    }

    // 何も選択されていない場合はスルー。
    if (choices != null) {
      lastState.setState(lastQuestion.isCorrect(choices));
    }
  }
}
