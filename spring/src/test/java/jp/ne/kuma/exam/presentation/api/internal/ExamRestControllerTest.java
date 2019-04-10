package jp.ne.kuma.exam.presentation.api.internal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.ne.kuma.exam.common.annotation.UnitTest;
import jp.ne.kuma.exam.common.filter.MockMvcRequestHeaderFilter;
import jp.ne.kuma.exam.common.util.TestUtil;
import jp.ne.kuma.exam.persistence.dto.Exam;
import jp.ne.kuma.exam.presentation.form.ExamForm;
import jp.ne.kuma.exam.presentation.validator.ExamFormValidator;
import jp.ne.kuma.exam.service.EditQuestionService;
import jp.ne.kuma.exam.service.InitializeService;
import jp.ne.kuma.exam.service.ValidationMessageResolveService;

/**
 * 試験種別関連 REST コントローラのユニットテスト
 *
 * @author Mike
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@UnitTest
public class ExamRestControllerTest {

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
  private ExamRestController controller;
  /** 初期化サービス */
  @MockBean
  private InitializeService initializeService;
  /** 出題データメンテナンスサービス */
  @MockBean
  private EditQuestionService service;
  /** 試験情報作成編集画面入力情報バリデータ */
  @SpyBean
  private ExamFormValidator examValidator;
  /** 入力エラーメッセージ変換サービス */
  @MockBean
  private ValidationMessageResolveService messageResolver;
  /** フォームの引数キャプチャ */
  @Captor
  private ArgumentCaptor<ExamForm> formCaptor;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .addFilters(context.getBean(SessionRepositoryFilter.class))
        .apply(springSecurity()).build();
  }

  @Test
  public void 全ての試験情報を取得() throws Exception {

    // 想定値設定
    List<Exam> expected = TestUtil.excel.loadAsPojoList(Exam.class, this.getClass(), "Exam", "Data1");

    // モック設定
    doReturn(expected).when(initializeService).getAllExams();

    // 試験実行
    mockMvc.perform(get("/api/internal/exam"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(initializeService, times(1)).getAllExams();
  }

  @Test
  public void 試験種別データを取得() throws Exception {

    // 想定値設定
    final int examNo = 2;
    final Optional<Exam> expected = Optional.ofNullable(
        TestUtil.excel.loadAsPojo(Exam.class, this.getClass(), "Exam", "Data2"));

    // モック設定
    doReturn(expected).when(initializeService).getExactExam(anyInt());

    // 試験実行
    mockMvc.perform(get("/api/internal/exam/{examNo}", examNo))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected.get())));

    // 検証
    verify(initializeService, times(1)).getExactExam(examNo);
  }

  @Test
  @WithMockUser
  public void 入力情報バリデーション() throws Exception {

    // 想定値設定
    final ExamForm form = TestUtil.excel.loadAsPojo(ExamForm.class, this.getClass(), "ExamForm", "Data1");
    Map<String, List<String>> expected = new HashMap<>();

    // モック設定
    doReturn(true).when(examValidator).supports(any());
    doNothing().when(examValidator).validate(formCaptor.capture(), any());
    doReturn(expected).when(messageResolver).resolve(any(), any());

    // 試験実行
    mockMvc.perform(post("/api/internal/exam/validate")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(examValidator, times(1)).supports(ExamForm.class);
    verify(examValidator, times(1)).validate(any(), any());
    assertThat(formCaptor.getValue()).isEqualTo(form);
    verify(messageResolver, times(1)).resolve(any(), any());
  }

  @Test
  @WithMockUser
  public void 試験種別データの新規登録() throws Exception {

    // 想定値設定
    final ExamForm form = TestUtil.excel.loadAsPojo(ExamForm.class, this.getClass(), "ExamForm", "Data1");

    // モック設定
    doReturn(true).when(examValidator).supports(any());
    doNothing().when(examValidator).validate(formCaptor.capture(), any());
    doNothing().when(service).insertExam(any(ExamForm.class));

    // 試験実行
    mockMvc.perform(put("/api/internal/exam")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(examValidator, times(1)).supports(ExamForm.class);
    verify(examValidator, times(1)).validate(any(), any());
    assertThat(formCaptor.getValue()).isEqualTo(form);
    verify(service, times(1)).insertExam(form);
  }

  @Test
  @WithMockUser
  public void 試験種別データの新規登録時にバリデーションエラー() throws Exception {

    // 想定値設定
    final ExamForm form = new ExamForm();

    // 試験実行
    mockMvc.perform(put("/api/internal/exam")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isInternalServerError());

    // サービスが呼び出されない検証
    verify(service, never()).insertExam(any(ExamForm.class));
  }

  @Test
  @WithMockUser
  public void 試験種別データの更新登録() throws Exception {

    // 想定値設定
    final ExamForm form = TestUtil.excel.loadAsPojo(ExamForm.class, this.getClass(), "ExamForm", "Data2");

    // モック設定
    doReturn(true).when(examValidator).supports(any());
    doNothing().when(examValidator).validate(formCaptor.capture(), any());
    doNothing().when(service).updateExam(any(ExamForm.class));

    // 試験実行
    mockMvc.perform(post("/api/internal/exam")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(examValidator, times(1)).supports(ExamForm.class);
    verify(examValidator, times(1)).validate(any(), any());
    assertThat(formCaptor.getValue()).isEqualTo(form);
    verify(service, times(1)).updateExam(form);
  }

  @Test
  @WithMockUser
  public void 試験種別データの更新登録時にバリデーションエラー() throws Exception {

    // 想定値設定
    final ExamForm form = new ExamForm();

    // 試験実行
    mockMvc.perform(post("/api/internal/exam")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isInternalServerError());

    // サービスが呼び出されない検証
    verify(service, never()).updateExam(any(ExamForm.class));
  }

  @Test
  @WithMockUser
  public void 試験種別データ削除() throws Exception {

    // 想定値設定
    final int examNo = 2;

    // モック設定
    doNothing().when(service).deleteExam(anyInt());

    // 試験実行
    mockMvc.perform(delete("/api/internal/exam/{examNo}", examNo)
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(service, times(1)).deleteExam(examNo);
  }
}
