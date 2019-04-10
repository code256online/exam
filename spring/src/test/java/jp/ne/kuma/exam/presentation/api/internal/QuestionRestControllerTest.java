package jp.ne.kuma.exam.presentation.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.ne.kuma.exam.common.annotation.UnitTest;
import jp.ne.kuma.exam.common.bean.Pager;
import jp.ne.kuma.exam.common.bean.QAHistory;
import jp.ne.kuma.exam.common.bean.QAHistory.QuestionPageInfo;
import jp.ne.kuma.exam.common.bean.UserInfo;
import jp.ne.kuma.exam.common.enumerator.AnswerStates;
import jp.ne.kuma.exam.common.enumerator.QuestionMode;
import jp.ne.kuma.exam.common.filter.MockMvcRequestHeaderFilter;
import jp.ne.kuma.exam.common.util.TestUtil;
import jp.ne.kuma.exam.presentation.form.InitForm;
import jp.ne.kuma.exam.presentation.validator.InitFormValidator;
import jp.ne.kuma.exam.service.HistoryService;
import jp.ne.kuma.exam.service.InitializeService;
import jp.ne.kuma.exam.service.QuestionService;
import jp.ne.kuma.exam.service.ValidationMessageResolveService;
import jp.ne.kuma.exam.service.dto.Choice;
import jp.ne.kuma.exam.service.dto.HistoryItem;
import jp.ne.kuma.exam.service.dto.Question;
import jp.ne.kuma.exam.service.dto.QuestionData;

/**
 * 出題関連 REST コントローラのユニットテスト
 *
 * @author Mike
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@UnitTest
public class QuestionRestControllerTest {

  /** MVC モック */
  private MockMvc mockMvc;
  /** アプリケーションコンテキスト */
  @Autowired
  private WebApplicationContext context;
  /** JSON マッパー */
  @Autowired
  private ObjectMapper mapper;
  /** 試験対象クラス */
  @InjectMocks
  private QuestionRestController controller;
  /** 出題関連サービス */
  @MockBean
  private QuestionService questionService;
  /** 初期化サービス */
  @MockBean
  private InitializeService initializeService;
  /** 出題初期化画面入力情報バリデータ */
  @SpyBean
  private InitFormValidator initValidator;
  /** 履歴関連サービス */
  @MockBean
  private HistoryService historyService;
  /** このセッションの回答履歴 */
  @MockBean
  private QAHistory history;
  /** バリデーションエラーメッセージ変換サービス */
  @MockBean
  private ValidationMessageResolveService messageResolver;
  /** 時刻オブジェクト */
  @MockBean
  private Clock clock;
  /** ログインユーザー情報 */
  @MockBean
  private UserInfo userInfo;
  /** フォームの引数キャプチャ */
  @Captor
  private ArgumentCaptor<InitForm> formCaptor;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .addFilters(context.getBean(SessionRepositoryFilter.class))
        .apply(springSecurity()).build();
  }

  @Test
  @WithMockUser
  public void 回答と出題() throws Exception {

    // 想定値設定
    final int page = 1;
    final String r = "A";
    List<QuestionPageInfo> answerStates = TestUtil.excel.loadAsPojoList(QuestionPageInfo.class, this.getClass(),
        "AnswerStates", "Data1");
    final Pageable pageable = PageRequest.of(answerStates.get(page).getQuestionNo(), 1,
        Sort.by(Direction.ASC, "question_no"));
    final Pageable nextPage = PageRequest.of(page, 1, Sort.by(Direction.ASC, "question_no"));
    List<Question> lastContent = TestUtil.excel.loadAsPojoList(Question.class, this.getClass(), "Question", "Data1");
    lastContent.get(0).setChoices(IntStream.range(0, 4).boxed().map(Choice::new).collect(Collectors.toList()));
    Page<Question> lastQuestion = Pager.of(lastContent).pageable(PageRequest.of(0, 1)).totalElements(10).build();
    List<Question> content = TestUtil.excel.loadAsPojoList(Question.class, this.getClass(), "Question", "Data2");
    content.get(0).setChoices(IntStream.range(0, 5).boxed().map(Choice::new).collect(Collectors.toList()));
    Page<Question> expected = Pager.of(content).pageable(nextPage).totalElements(10).build();

    // モック設定
    doReturn(answerStates).when(history).getAnswerStates();
    doReturn(lastQuestion).when(history).getLastQuestion();
    doReturn(expected).when(questionService).getPage(any(QAHistory.class), any(Pageable.class), any(Pageable.class));
    doNothing().when(history).setLastQuestion(any());

    // 試験実行
    mockMvc.perform(get("/api/internal/question")
        .param("page", String.valueOf(page)).param("r", r)
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(history, atLeastOnce()).getAnswerStates();
    verify(history, atLeastOnce()).getLastQuestion();
    assertThat(answerStates.get(0).getState()).isEqualTo(AnswerStates.CORRECT);
    verify(questionService, times(1)).getPage(history, pageable, nextPage);
    verify(history, times(1)).setLastQuestion(expected);
  }

  @Test
  @WithMockUser
  public void 回答と出題_リクエストされたページがマイナス１なら最終ページを返す() throws Exception {

    // 想定値設定
    final int page = -1;
    final String[] c = { "A", "C" };
    List<QuestionPageInfo> answerStates = TestUtil.excel.loadAsPojoList(QuestionPageInfo.class, this.getClass(),
        "AnswerStates", "Data2");
    final Pageable pageable = PageRequest.of(answerStates.get(answerStates.size() - 1).getQuestionNo(), 1,
        Sort.by(Direction.ASC, "question_no"));
    final Pageable nextPage = PageRequest.of((answerStates.size() - 1), 1, Sort.by(Direction.ASC, "question_no"));
    List<Question> lastContent = TestUtil.excel.loadAsPojoList(Question.class, this.getClass(), "Question", "Data2");
    lastContent.get(0).setChoices(IntStream.range(0, 5).boxed().map(Choice::new).collect(Collectors.toList()));
    Page<Question> lastQuestion = Pager.of(lastContent).pageable(PageRequest.of(0, 1)).totalElements(10).build();
    List<Question> content = TestUtil.excel.loadAsPojoList(Question.class, this.getClass(), "Question", "Data3");
    content.get(0).setChoices(IntStream.range(0, 6).boxed().map(Choice::new).collect(Collectors.toList()));
    Page<Question> expected = Pager.of(content).pageable(nextPage).totalElements(10).build();

    // モック設定
    doReturn(answerStates).when(history).getAnswerStates();
    doReturn(lastQuestion).when(history).getLastQuestion();
    doReturn(expected).when(questionService).getPage(any(QAHistory.class), any(Pageable.class), any(Pageable.class));
    doNothing().when(history).setLastQuestion(any());

    // 試験実行
    mockMvc.perform(get("/api/internal/question")
        .param("page", String.valueOf(page)).param("c", c)
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(history, atLeastOnce()).getAnswerStates();
    verify(history, atLeastOnce()).getLastQuestion();
    assertThat(answerStates.get(1).getState()).isEqualTo(AnswerStates.INCORRECT);
    verify(questionService, times(1)).getPage(history, pageable, nextPage);
    verify(history, times(1)).setLastQuestion(expected);
  }

  @Test
  @WithMockUser
  public void レジューム確認() throws Exception {

    // 想定値設定
    final boolean expected = false;
    final Page<Question> lastQuestion = null;

    // モック設定
    doReturn(lastQuestion).when(history).getLastQuestion();

    // 試験実行
    mockMvc.perform(get("/api/internal/question/resumeCheck")
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string(String.valueOf(expected)));

    // 検証
    verify(history, times(1)).getLastQuestion();
  }

  @Test
  @WithMockUser
  public void 出題履歴リセット() throws Exception {

    // モック設定
    doNothing().when(history).reset();

    // 試験実行
    mockMvc.perform(get("/api/internal/question/reset")
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(history, times(1)).reset();
  }

  @Test
  @WithMockUser
  public void 初期化時のバリデーション() throws Exception {

    // 想定値設定
    final InitForm form = TestUtil.excel.loadAsPojo(InitForm.class, this.getClass(), "InitForm", "Data1");
    Map<String, List<String>> expected = new HashMap<>();

    // モック設定
    doReturn(true).when(initValidator).supports(any());
    doNothing().when(initValidator).validate(formCaptor.capture(), any());
    doReturn(expected).when(messageResolver).resolve(any(), any());

    // 試験実行
    mockMvc.perform(post("/api/internal/question/initValidation")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(initValidator, times(1)).supports(InitForm.class);
    verify(initValidator, times(1)).validate(any(), any());
    assertThat(formCaptor.getValue()).isEqualTo(form);
    verify(messageResolver, times(1)).resolve(any(), any());
  }

  @Test
  @WithMockUser
  public void 出題画面初期化_通常出題で出題数は種別ごと最大数未満() throws Exception {

    // 想定値設定
    final InitForm form = TestUtil.excel.loadAsPojo(InitForm.class, this.getClass(), "InitForm", "Data1");
    final LocalDateTime now = LocalDateTime.of(2019, 1, 28, 12, 34, 56);
    final int examineeId = 1;
    final int count = 81;
    List<QuestionPageInfo> answerStates = TestUtil.excel.loadAsPojoList(QuestionPageInfo.class, this.getClass(),
        "AnswerStates", "Data3");
    Set<Integer> randomNumbers = answerStates.stream().map(x -> x.getQuestionNo()).collect(Collectors.toSet());
    final Pageable pageable = PageRequest.of(answerStates.get(0).getQuestionNo(), 1, Sort.by(Direction.ASC, "question_no"));
    final Pageable nextPage = PageRequest.of(0, 1, Sort.by(Direction.ASC, "question_no"));
    List<Question> content = TestUtil.excel.loadAsPojoList(Question.class, this.getClass(), "Question", "Data1");
    content.get(0).setChoices(IntStream.range(0, 4).boxed().map(Choice::new).collect(Collectors.toList()));
    Page<Question> expected = Pager.of(content).pageable(nextPage).totalElements(randomNumbers.size()).build();

    // モック設定
    doReturn(true).when(initValidator).supports(any());
    doNothing().when(initValidator).validate(formCaptor.capture(), any());
    doReturn(null).when(history).getLastQuestion();
    doReturn(now.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
    doReturn(ZoneId.systemDefault()).when(clock).getZone();
    doNothing().when(history).setStartDatetime(any(LocalDateTime.class));
    doReturn(examineeId).when(userInfo).getId();
    doNothing().when(history).setExamineeId(anyInt());
    doNothing().when(history).setExamNo(anyInt());
    doNothing().when(history).setQuestionMode(any(QuestionMode.class));
    doNothing().when(history).setExamCoverage(anyInt());
    doReturn(count).when(questionService).getCount(anyInt(), anyInt());
    doReturn(randomNumbers).when(questionService).generateUniqueRandomNumbers(anyInt(), anyInt());
    doNothing().when(history).setAnswerStates(anyList());
    doReturn(answerStates).when(history).getAnswerStates();
    doReturn(expected).when(questionService).getPage(any(QAHistory.class), any(Pageable.class), any(Pageable.class));
    doNothing().when(history).setLastQuestion(any());
    doNothing().when(history).setQuestionCount(anyInt());

    // 試験実行
    mockMvc.perform(post("/api/internal/question/init")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(initValidator, times(1)).supports(InitForm.class);
    verify(initValidator, times(1)).validate(any(), any());
    assertThat(formCaptor.getValue()).isEqualTo(form);
    verify(history, times(1)).getLastQuestion();
    verify(clock, times(1)).instant();
    verify(clock, times(1)).getZone();
    verify(history, times(1)).setStartDatetime(now);
    verify(userInfo, times(1)).getId();
    verify(history, times(1)).setExamineeId(examineeId);
    verify(history, times(1)).setExamNo(form.getExamNo());
    verify(history, times(1)).setExamCoverage(form.getExamCoverage());
    verify(questionService, times(1)).getCount(form.getExamNo(), form.getExamCoverage());
    verify(questionService, times(1)).generateUniqueRandomNumbers(count, form.getQuestionCount());
    verify(history, times(1)).setAnswerStates(answerStates);
    verify(history, times(1)).getAnswerStates();
    verify(questionService, times(1)).getPage(history, pageable, nextPage);
    verify(history, times(1)).setLastQuestion(expected);
    verify(history, times(1)).setQuestionCount(randomNumbers.size());
  }

  @Test
  @WithMockUser
  public void 出題画面初期化_通常出題で出題数は種別ごと最大数を超える() throws Exception {

    // 想定値設定
    final InitForm form = TestUtil.excel.loadAsPojo(InitForm.class, this.getClass(), "InitForm", "Data1");
    final LocalDateTime now = LocalDateTime.of(2019, 1, 28, 12, 34, 56);
    final int examineeId = 1;
    final int count = 4;
    List<QuestionPageInfo> answerStates = TestUtil.excel.loadAsPojoList(QuestionPageInfo.class, this.getClass(),
        "AnswerStates", "Data4");
    Set<Integer> randomNumbers = answerStates.stream().map(x -> x.getQuestionNo()).collect(Collectors.toSet());
    final Pageable pageable = PageRequest.of(answerStates.get(0).getQuestionNo(), 1, Sort.by(Direction.ASC, "question_no"));
    final Pageable nextPage = PageRequest.of(0, 1, Sort.by(Direction.ASC, "question_no"));
    List<Question> content = TestUtil.excel.loadAsPojoList(Question.class, this.getClass(), "Question", "Data1");
    content.get(0).setChoices(IntStream.range(0, 4).boxed().map(Choice::new).collect(Collectors.toList()));
    Page<Question> expected = Pager.of(content).pageable(nextPage).totalElements(answerStates.size()).build();

    // モック設定
    doReturn(true).when(initValidator).supports(any());
    doNothing().when(initValidator).validate(formCaptor.capture(), any());
    doReturn(null).when(history).getLastQuestion();
    doReturn(now.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
    doReturn(ZoneId.systemDefault()).when(clock).getZone();
    doNothing().when(history).setStartDatetime(any(LocalDateTime.class));
    doReturn(examineeId).when(userInfo).getId();
    doNothing().when(history).setExamineeId(anyInt());
    doNothing().when(history).setExamNo(anyInt());
    doNothing().when(history).setQuestionMode(any(QuestionMode.class));
    doNothing().when(history).setExamCoverage(anyInt());
    doReturn(count).when(questionService).getCount(anyInt(), anyInt());
    doReturn(randomNumbers).when(questionService).generateUniqueRandomNumbers(anyInt(), anyInt());
    doNothing().when(history).setAnswerStates(anyList());
    doReturn(answerStates).when(history).getAnswerStates();
    doReturn(expected).when(questionService).getPage(any(QAHistory.class), any(Pageable.class), any(Pageable.class));
    doNothing().when(history).setLastQuestion(any());
    doNothing().when(history).setQuestionCount(anyInt());

    // 試験実行
    mockMvc.perform(post("/api/internal/question/init")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(initValidator, times(1)).supports(InitForm.class);
    verify(initValidator, times(1)).validate(any(), any());
    assertThat(formCaptor.getValue()).isEqualTo(form);
    verify(history, times(1)).getLastQuestion();
    verify(clock, times(1)).instant();
    verify(clock, times(1)).getZone();
    verify(history, times(1)).setStartDatetime(now);
    verify(userInfo, times(1)).getId();
    verify(history, times(1)).setExamineeId(examineeId);
    verify(history, times(1)).setExamNo(form.getExamNo());
    verify(history, times(1)).setExamCoverage(form.getExamCoverage());
    verify(questionService, times(1)).getCount(form.getExamNo(), form.getExamCoverage());
    verify(questionService, times(1)).generateUniqueRandomNumbers(count, count);
    verify(history, times(1)).setAnswerStates(answerStates);
    verify(history, times(1)).getAnswerStates();
    verify(questionService, times(1)).getPage(history, pageable, nextPage);
    verify(history, times(1)).setLastQuestion(expected);
    verify(history, times(1)).setQuestionCount(answerStates.size());
  }

  @Test
  @WithMockUser
  public void 出題画面初期化_固定出題モード() throws Exception {

    final InitForm form = TestUtil.excel.loadAsPojo(InitForm.class, this.getClass(), "InitForm", "Data2");
    final LocalDateTime now = LocalDateTime.of(2019, 1, 28, 12, 34, 56);
    final int examineeId = 1;
    List<QuestionData> questions = TestUtil.excel.loadAsPojoList(QuestionData.class, this.getClass(),
        "QuestionData", "Data1");
    List<QuestionPageInfo> answerStates = TestUtil.excel.loadAsPojoList(QuestionPageInfo.class, this.getClass(),
        "AnswerStates", "Data5");
    final Pageable pageable = PageRequest.of(answerStates.get(0).getQuestionNo(), 1, Sort.by(Direction.ASC, "question_no"));
    final Pageable nextPage = PageRequest.of(0, 1, Sort.by(Direction.ASC, "question_no"));
    List<Question> content = TestUtil.excel.loadAsPojoList(Question.class, this.getClass(), "Question", "Data1");
    content.get(0).setChoices(IntStream.range(0, 4).boxed().map(Choice::new).collect(Collectors.toList()));
    Page<Question> expected = Pager.of(content).pageable(nextPage).totalElements(answerStates.size()).build();

    // モック設定
    doReturn(true).when(initValidator).supports(any());
    doNothing().when(initValidator).validate(formCaptor.capture(), any());
    doReturn(null).when(history).getLastQuestion();
    doReturn(now.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
    doReturn(ZoneId.systemDefault()).when(clock).getZone();
    doNothing().when(history).setStartDatetime(any(LocalDateTime.class));
    doReturn(examineeId).when(userInfo).getId();
    doNothing().when(history).setExamineeId(anyInt());
    doNothing().when(history).setFixedQuestionsId(anyInt());
    doReturn(questions).when(initializeService).getFixedQuestionNumbers(anyInt());
    doNothing().when(history).setAnswerStates(anyList());
    doReturn(answerStates).when(history).getAnswerStates();
    doReturn(expected).when(questionService).getPage(any(QAHistory.class), any(Pageable.class), any(Pageable.class));
    doNothing().when(history).setLastQuestion(any());
    doNothing().when(history).setQuestionCount(anyInt());

    // 試験実行
    mockMvc.perform(post("/api/internal/question/init")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(initValidator, times(1)).supports(InitForm.class);
    verify(initValidator, times(1)).validate(any(), any());
    assertThat(formCaptor.getValue()).isEqualTo(form);
    verify(history, times(1)).getLastQuestion();
    verify(clock, times(1)).instant();
    verify(clock, times(1)).getZone();
    verify(history, times(1)).setStartDatetime(now);
    verify(userInfo, times(1)).getId();
    verify(history, times(1)).setExamineeId(examineeId);
    verify(history, times(1)).setFixedQuestionsId(form.getFixedQuestionsId());
    verify(initializeService, times(1)).getFixedQuestionNumbers(form.getFixedQuestionsId());
    verify(history, times(1)).setAnswerStates(answerStates);
    verify(history, times(1)).getAnswerStates();
    verify(questionService, times(1)).getPage(history, pageable, nextPage);
    verify(history, times(1)).setLastQuestion(expected);
    verify(history, times(1)).setQuestionCount(answerStates.size());
  }

  @Test
  @WithMockUser
  public void 回答終了して履歴書き込み() throws Exception {

    // 想定値設定
    List<Question> lastContent = TestUtil.excel.loadAsPojoList(Question.class, this.getClass(),
        "Question", "Data1");
    lastContent.get(0).setChoices(IntStream.range(0, 4).boxed().map(Choice::new).collect(Collectors.toList()));
    Page<Question> lastQuestion = Pager.of(lastContent).pageable(PageRequest.of(0, 1)).totalElements(10).build();
    List<QuestionPageInfo> answerStates = TestUtil.excel.loadAsPojoList(QuestionPageInfo.class, this.getClass(),
        "AnswerStates", "Data1");

    // モック設定
    doReturn(lastQuestion).when(history).getLastQuestion();
    doReturn(answerStates).when(history).getAnswerStates();
    doNothing().when(historyService).update(any(QAHistory.class));

    // 試験実行
    mockMvc.perform(get("/api/internal/question/finish")
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(history, times(1)).getLastQuestion();
    assertThat(answerStates.get(0).getState()).isEqualTo(AnswerStates.NOT_ANSWERED);
    verify(historyService, times(1)).update(history);
  }

  @Test
  @WithMockUser
  public void 結果出力() throws Exception {

    // 想定値設定
    final int examineeId = 1;
    final Pageable pageable = PageRequest.of(0, 10);
    final QuestionMode questionMode = QuestionMode.NORMAL;
    List<HistoryItem> content = TestUtil.excel.loadAsPojoList(HistoryItem.class, this.getClass(),
        "HistoryItem", "Data1");
    Page<HistoryItem> expected = Pager.of(content).pageable(pageable).totalElements(content.size()).build();

    // モック設定
    doReturn(examineeId).when(history).getExamineeId();
    doReturn(questionMode).when(history).getQuestionMode();
    doReturn(expected).when(historyService).getFinishPage(anyInt(), any(Pageable.class), any(QuestionMode.class));

    // 試験実行
    mockMvc.perform(get("/api/internal/question/finishPage")
        .with(new MockMvcRequestHeaderFilter(context)))
    .andDo(print())
    .andExpect(status().isOk())
    .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(history, times(1)).getExamineeId();
    verify(history, times(1)).getQuestionMode();
    verify(historyService, times(1)).getFinishPage(examineeId, pageable, questionMode);
  }
}
