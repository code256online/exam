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
import jp.ne.kuma.exam.persistence.dto.ExamCoverage;
import jp.ne.kuma.exam.presentation.form.ExamCoverageForm;
import jp.ne.kuma.exam.presentation.validator.ExamCoverageFormValidator;
import jp.ne.kuma.exam.service.EditQuestionService;
import jp.ne.kuma.exam.service.InitializeService;
import jp.ne.kuma.exam.service.ValidationMessageResolveService;

/**
 * 試験範囲関連 REST コントローラのユニットテスト
 *
 * @author Mike
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@UnitTest
public class ExamCoverageRestControllerTest {

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
  private ExamCoverageRestController controller;
  /** 初期化サービス */
  @MockBean
  private InitializeService initializeService;
  /** 出題データメンテナンスサービス */
  @MockBean
  private EditQuestionService service;
  /** 試験範囲入力情報バリデータ */
  @SpyBean
  private ExamCoverageFormValidator coverageValidator;
  /** 入力エラーメッセージ変換サービス */
  @MockBean
  private ValidationMessageResolveService messageResolver;
  /** フォームの引数キャプチャ */
  @Captor
  private ArgumentCaptor<ExamCoverageForm> formCaptor;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .addFilters(context.getBean(SessionRepositoryFilter.class))
        .apply(springSecurity()).build();
  }

  @Test
  public void すべての試験範囲情報を取得() throws Exception {

    // 想定値設定
    final int examNo = 1;
    final boolean includeDeleted = false;
    List<ExamCoverage> expected = TestUtil.excel.loadAsPojoList(ExamCoverage.class, this.getClass(),
        "ExamCoverage", "Data1");

    // モック設定
    doReturn(expected).when(initializeService).getCoverages(anyInt(), anyBoolean());

    // 試験実行
    mockMvc.perform(get("/api/internal/coverage/{examNo}", examNo))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(initializeService, times(1)).getCoverages(examNo, includeDeleted);
  }

  @Test
  public void 廃止フラグ設定済みを含めてすべての試験範囲情報を取得() throws Exception {

    // 想定値設定
    final int examNo = 2;
    final boolean includeDeleted = true;
    List<ExamCoverage> expected = TestUtil.excel.loadAsPojoList(ExamCoverage.class, this.getClass(),
        "ExamCoverage", "Data2");

    // モック設定
    doReturn(expected).when(initializeService).getCoverages(anyInt(), anyBoolean());

    // 試験実行
    mockMvc.perform(get("/api/internal/coverage/includeDeleted/{examNo}", examNo))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(initializeService, times(1)).getCoverages(examNo, includeDeleted);
  }

  @Test
  public void 主キーで試験範囲情報を取得() throws Exception {

    // 想定値設定
    final int examNo = 1;
    final int id = 2;
    final Optional<ExamCoverage> expected = Optional.ofNullable(
        TestUtil.excel.loadAsPojo(ExamCoverage.class, this.getClass(), "ExamCoverage", "Data3"));

    // モック設定
    doReturn(expected).when(initializeService).getExactExamCoverage(anyInt(), anyInt());

    // 試験実行
    mockMvc.perform(get("/api/internal/coverage/{examNo}/{id}", examNo, id))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected.get())));

    // 検証
    verify(initializeService, times(1)).getExactExamCoverage(examNo, id);
  }

  @Test
  @WithMockUser
  public void 入力バリデーション() throws Exception {

    // 想定値設定
    final ExamCoverageForm form = TestUtil.excel.loadAsPojo(ExamCoverageForm.class, this.getClass(),
        "ExamCoverageForm", "Data1");
    Map<String, List<String>> expected = new HashMap<>();

    // モック設定
    doReturn(true).when(coverageValidator).supports(any());
    doNothing().when(coverageValidator).validate(formCaptor.capture(), any());
    doReturn(expected).when(messageResolver).resolve(any(), any());

    // 試験実行
    mockMvc.perform(post("/api/internal/coverage/validate")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(coverageValidator, times(1)).supports(ExamCoverageForm.class);
    verify(coverageValidator, times(1)).validate(any(ExamCoverageForm.class), any());
    assertThat(formCaptor.getValue()).isEqualTo(form);
    verify(messageResolver, times(1)).resolve(any(), any());
  }

  @Test
  @WithMockUser
  public void 試験範囲データ新規登録() throws Exception {

    // 想定値設定
    final ExamCoverageForm form = TestUtil.excel.loadAsPojo(ExamCoverageForm.class, this.getClass(),
        "ExamCoverageForm", "Data1");

    // モック設定
    doReturn(true).when(coverageValidator).supports(any());
    doNothing().when(coverageValidator).validate(formCaptor.capture(), any());
    doNothing().when(service).insertExamCoverage(any(ExamCoverageForm.class));

    // 試験実行
    mockMvc.perform(put("/api/internal/coverage")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(coverageValidator, times(1)).supports(ExamCoverageForm.class);
    verify(coverageValidator, times(1)).validate(any(ExamCoverageForm.class), any());
    assertThat(formCaptor.getValue()).isEqualTo(form);
    verify(service, times(1)).insertExamCoverage(form);
  }

  @Test
  @WithMockUser
  public void 試験範囲データ新規登録時にバリデーションエラー() throws Exception {

    // 想定値設定
    final ExamCoverageForm form = new ExamCoverageForm();

    // 試験実行
    mockMvc.perform(put("/api/internal/coverage")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isInternalServerError());

    // サービスが呼び出されない検証
    verify(service, never()).insertExamCoverage(any(ExamCoverageForm.class));
  }

  @Test
  @WithMockUser
  public void 試験範囲データ更新登録() throws Exception {

    // 想定値設定
    final ExamCoverageForm form = TestUtil.excel.loadAsPojo(ExamCoverageForm.class, this.getClass(),
        "ExamCoverageForm", "Data2");

    // モック設定
    doReturn(true).when(coverageValidator).supports(any());
    doNothing().when(coverageValidator).validate(formCaptor.capture(), any());
    doNothing().when(service).updateExamCoverage(any(ExamCoverageForm.class));

    // 試験実行
    mockMvc.perform(post("/api/internal/coverage")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(coverageValidator, times(1)).supports(ExamCoverageForm.class);
    verify(coverageValidator, times(1)).validate(any(ExamCoverageForm.class), any());
    assertThat(formCaptor.getValue()).isEqualTo(form);
    verify(service, times(1)).updateExamCoverage(form);
  }

  @Test
  @WithMockUser
  public void 試験範囲データ更新登録時にバリデーションエラー() throws Exception {

    // 想定値設定
    final ExamCoverageForm form = new ExamCoverageForm();

    // 試験実行
    mockMvc.perform(post("/api/internal/coverage")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isInternalServerError());

    // サービスが呼び出されない検証
    verify(service, never()).updateExamCoverage(form);
  }

  @Test
  @WithMockUser
  public void 試験範囲データ削除() throws Exception {

    // 想定値設定
    final int examNo = 1;
    final int id = 2;

    // モック設定
    doNothing().when(service).deleteExamCoverage(anyInt(), anyInt());

    // 試験実行
    mockMvc.perform(delete("/api/internal/coverage/{examNo}/{id}", examNo, id)
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(service, times(1)).deleteExamCoverage(examNo, id);
  }
}
