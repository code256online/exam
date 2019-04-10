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
import jp.ne.kuma.exam.presentation.form.FixedQuestionForm;
import jp.ne.kuma.exam.presentation.validator.FixedQuestionFormValidator;
import jp.ne.kuma.exam.service.EditQuestionService;
import jp.ne.kuma.exam.service.InitializeService;
import jp.ne.kuma.exam.service.ValidationMessageResolveService;

/**
 * 固定出題関連 REST コントローラのユニットテスト
 *
 * @author Mike
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@UnitTest
public class FixedQuestionsRestControllerTest {

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
  private FixedQuestionsRestController controller;
  /** 初期化サービス */
  @MockBean
  private InitializeService initService;
  /** 出題データメンテナンスサービス */
  @MockBean
  private EditQuestionService service;
  /** 固定出題データ作成編集画面入力情報バリデータ */
  @SpyBean
  private FixedQuestionFormValidator fixedQuestionValidator;
  /** 画面入力エラー情報変換サービス */
  @MockBean
  private ValidationMessageResolveService messageResolver;
  @Captor
  private ArgumentCaptor<FixedQuestionForm> formCaptor;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .addFilters(context.getBean(SessionRepositoryFilter.class))
        .apply(springSecurity()).build();
  }

  @Test
  public void 全ての固定出題データを取得() throws Exception {

    // 想定値設定
    List<FixedQuestionForm> expected = TestUtil.excel.loadAsPojoList(FixedQuestionForm.class, this.getClass(),
        "FixedQuestionForm", "Data1");

    // モック設定
    doReturn(expected).when(initService).getFixedQuestions();

    // 試験実行
    mockMvc.perform(get("/api/internal/fixed"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(initService, times(1)).getFixedQuestions();
  }

  @Test
  public void プライマリキーで固定出題データを取得() throws Exception {

    // 想定値設定
    final int id = 2;
    final FixedQuestionForm expected = TestUtil.excel.loadAsPojo(FixedQuestionForm.class, this.getClass(),
        "FixedQuestionForm", "Data2");

    // モック設定
    doReturn(Optional.ofNullable(expected)).when(initService).getExactFixedQuestion(anyInt());

    // 試験実行
    mockMvc.perform(get("/api/internal/fixed/{id}", id))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(initService, times(1)).getExactFixedQuestion(id);
  }

  @Test
  @WithMockUser
  public void 画面入力情報のバリデーション() throws Exception {

    // 想定値設定
    final FixedQuestionForm form = TestUtil.excel.loadAsPojo(FixedQuestionForm.class, this.getClass(),
        "FixedQuestionForm", "Data3");
    Map<String, List<String>> expected = new HashMap<>();

    // モック設定
    doReturn(true).when(fixedQuestionValidator).supports(any());
    doNothing().when(fixedQuestionValidator).validate(formCaptor.capture(), any());
    doReturn(expected).when(messageResolver).resolve(any(), any());

    // 試験実行
    mockMvc.perform(post("/api/internal/fixed/validate")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(fixedQuestionValidator, times(1)).supports(FixedQuestionForm.class);
    verify(fixedQuestionValidator, times(1)).validate(any(), any());
    assertThat(formCaptor.getValue()).isEqualTo(form);
    verify(messageResolver, times(1)).resolve(any(), any());
  }

  @Test
  @WithMockUser
  public void 固定出題データの新規作成() throws Exception {

    // 想定値設定
    final FixedQuestionForm form = TestUtil.excel.loadAsPojo(FixedQuestionForm.class, this.getClass(),
        "FixedQuestionForm", "Data3");

    // モック設定
    doReturn(true).when(fixedQuestionValidator).supports(any());
    doNothing().when(fixedQuestionValidator).validate(formCaptor.capture(), any());
    doReturn(2).when(service).insertFixedQuestion(any(FixedQuestionForm.class));

    // 試験実行
    mockMvc.perform(put("/api/internal/fixed")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(fixedQuestionValidator, times(1)).supports(FixedQuestionForm.class);
    verify(fixedQuestionValidator, times(1)).validate(any(), any());
    assertThat(formCaptor.getValue()).isEqualTo(form);
    verify(service, times(1)).insertFixedQuestion(form);
  }

  @Test
  @WithMockUser
  public void 固定出題データの新規作成時にバリデーションエラー() throws Exception {

    // 想定値設定
    final FixedQuestionForm form = new FixedQuestionForm();

    // 試験実行
    mockMvc.perform(put("/api/internal/fixed")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isInternalServerError());

    // サービスが呼び出されない検証
    verify(service, never()).insertFixedQuestion(any(FixedQuestionForm.class));
  }

  @Test
  @WithMockUser
  public void 固定出題データの更新登録() throws Exception {

    // 想定値設定
    final FixedQuestionForm form = TestUtil.excel.loadAsPojo(FixedQuestionForm.class, this.getClass(),
        "FixedQuestionForm", "Data4");

    // モック設定
    doReturn(true).when(fixedQuestionValidator).supports(any());
    doNothing().when(fixedQuestionValidator).validate(formCaptor.capture(), any());
    doNothing().when(service).updateFixedQuestion(any(FixedQuestionForm.class));

    // 試験実行
    mockMvc.perform(post("/api/internal/fixed")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(fixedQuestionValidator, times(1)).supports(FixedQuestionForm.class);
    verify(fixedQuestionValidator, times(1)).validate(any(), any());
    assertThat(formCaptor.getValue()).isEqualTo(form);
    verify(service, times(1)).updateFixedQuestion(form);
  }

  @Test
  @WithMockUser
  public void 固定出題データの更新登録時にバリデーションエラー() throws Exception {

    // 想定値設定
    final FixedQuestionForm form = new FixedQuestionForm();

    // 試験実行
    mockMvc.perform(post("/api/internal/fixed")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isInternalServerError());

    // サービスが呼び出されない検証
    verify(service, never()).updateFixedQuestion(any(FixedQuestionForm.class));
  }

  @Test
  @WithMockUser
  public void 固定出題データ削除() throws Exception {

    // 想定値設定
    final int id = 4;

    // モック設定
    doNothing().when(service).deleteFixedQuestion(anyInt());

    // 試験実行
    mockMvc.perform(delete("/api/internal/fixed/{id}", id)
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(service, times(1)).deleteFixedQuestion(id);
  }
}
