package jp.ne.kuma.exam.presentation.api.internal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import jp.ne.kuma.exam.common.filter.MockMvcRequestHeaderFilter;
import jp.ne.kuma.exam.common.util.TestUtil;
import jp.ne.kuma.exam.persistence.dto.Answer;
import jp.ne.kuma.exam.persistence.dto.AnswerPageDto;
import jp.ne.kuma.exam.presentation.form.AnswerForm;
import jp.ne.kuma.exam.presentation.validator.AnswerFormValidator;
import jp.ne.kuma.exam.service.EditQuestionService;
import jp.ne.kuma.exam.service.InitializeService;
import jp.ne.kuma.exam.service.ValidationMessageResolveService;

/**
 * AnswerRestController のユニットテストクラス
 *
 * @author Mike
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@UnitTest
public class AnswerRestControllerTest {

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
  private AnswerRestController controller;
  /** 初期化関連サービス */
  @MockBean
  private InitializeService initializeService;
  /** 出題データメンテナンス関連サービス */
  @MockBean
  private EditQuestionService service;
  /** 出題データ作成更新情報バリデータ */
  @SpyBean
  private AnswerFormValidator answerValidator;
  /** バリデーションメッセージ変換器 */
  @MockBean
  private ValidationMessageResolveService messageResolver;
  /** AnswerForm の引数キャプチャ */
  @Captor
  private ArgumentCaptor<AnswerForm> answerFormCaptor;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .addFilters(context.getBean(SessionRepositoryFilter.class))
        .apply(springSecurity()).build();
  }

  @Test
  public void 出題データメンテナンス画面での出題データ一覧ページを取得() throws Exception {

    // 想定値設定
    final int examNo = 1;
    final int page = 2;
    final Pageable pageable = PageRequest.of(page, 10, Sort.by(Direction.ASC, "question_no"));
    List<AnswerPageDto> content = TestUtil.excel.loadAsPojoList(AnswerPageDto.class, this.getClass(),
        "AnswerPageDto", "Data1");
    Page<AnswerPageDto> expected = Pager.of(content).pageable(pageable).totalElements(23).build();

    // モック設定
    doReturn(expected).when(initializeService).getAnswerPage(anyInt(), any(Pageable.class));

    // 試験実行
    mockMvc.perform(get("/api/internal/answer")
        .param("examNo", String.valueOf(examNo))
        .param("page", String.valueOf(page)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // サービス呼び出し検証
    verify(initializeService, times(1)).getAnswerPage(examNo, pageable);
  }

  @Test
  public void 試験番号と問題番号に紐付く出題データを取得() throws Exception {

    // 想定値設定
    final int examNo = 1;
    final int questionNo = 2;
    final Optional<Answer> expected = Optional.ofNullable(
        TestUtil.excel.loadAsPojo(Answer.class, this.getClass(), "Answer", "Data1"));

    // モック設定
    doReturn(expected).when(initializeService).getExactAnswer(anyInt(), anyInt());

    // 試験実行
    mockMvc.perform(get("/api/internal/answer/{examNo}/{questionNo}", examNo, questionNo))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected.get())));

    // サービス呼び出し検証
    verify(initializeService).getExactAnswer(examNo, questionNo);
  }

  @Test
  @WithMockUser
  public void 出題データ編集更新画面のバリデーション() throws Exception {

    // 想定値設定
    final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data1");
    final Map<String, List<String>> errors = new LinkedHashMap<>();

    // モック設定
    doReturn(true).when(answerValidator).supports(any());
    doNothing().when(answerValidator).validate(answerFormCaptor.capture(), any());
    doReturn(errors).when(messageResolver).resolve(any(), any());

    // 試験実行
    mockMvc.perform(post("/api/internal/answer/validate")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(errors)));

    // サービス呼び出し検証
    verify(answerValidator, times(1)).supports(AnswerForm.class);
    verify(answerValidator, times(1)).validate(any(), any());
    assertThat(answerFormCaptor.getValue()).isEqualTo(form);
    verify(messageResolver, times(1)).resolve(any(), any());
  }

  @Test
  public void 最大の問題番号を取得() throws Exception {

    // 想定値設定
    final int examNo = 1;
    final int expected = 81;

    // モック設定
    doReturn(expected).when(service).getMaxQuestionNo(anyInt());

    // 試験実行
    mockMvc.perform(get("/api/internal/answer/maxQuestionNo/{examNo}", examNo))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(service, times(1)).getMaxQuestionNo(examNo);
  }

  @Test
  public void 出題データ新規登録() throws Exception {

    // 想定値設定
    final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data1");

    // モック設定
    doNothing().when(answerValidator).validate(answerFormCaptor.capture(), any());
    doNothing().when(service).insertAnswer(any(AnswerForm.class));

    // 試験実行
    mockMvc.perform(put("/api/internal/answer")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(answerValidator, times(1)).validate(any(), any());
    assertThat(answerFormCaptor.getValue()).isEqualTo(form);
    verify(service, times(1)).insertAnswer(form);
  }

  @Test
  public void 出題データ新規登録時にバリデーションエラー() throws Exception {

    // 想定値設定
    final AnswerForm form = new AnswerForm();

    // 試験実行
    mockMvc.perform(put("/api/internal/answer")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isInternalServerError());

    // サービスが呼び出されていない検証
    verify(service, never()).insertAnswer(form);
  }

  @Test
  public void 出題データ更新() throws Exception {

    // 想定値設定
    final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data2");

    // モック設定
    doReturn(true).when(answerValidator).supports(any());
    doNothing().when(answerValidator).validate(answerFormCaptor.capture(), any());
    doNothing().when(service).updateAnswer(any(AnswerForm.class));

    // 試験実行
    mockMvc.perform(post("/api/internal/answer")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(answerValidator, times(1)).supports(AnswerForm.class);
    verify(answerValidator, times(1)).validate(any(), any());
    assertThat(answerFormCaptor.getValue()).isEqualTo(form);
    verify(service, times(1)).updateAnswer(form);
  }

  @Test
  public void 出題データ更新時にバリデーションエラー() throws Exception {

    // 想定値設定
    final AnswerForm form = new AnswerForm();

    // 試験実行
    mockMvc.perform(post("/api/internal/answer")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isInternalServerError());

    // サービスが呼び出されていない検証
    verify(service, never()).updateAnswer(form);
  }

  @Test
  public void 出題データ削除() throws Exception {

    // 想定値設定
    final int examNo = 1;
    final int questionNo = 2;

    // モック設定
    doNothing().when(service).deleteAnswer(anyInt(), anyInt());

    // 試験実行
    mockMvc.perform(delete("/api/internal/answer/{examNo}/{questionNo}", examNo, questionNo)
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(service, times(1)).deleteAnswer(examNo, questionNo);
  }
}
