package jp.ne.kuma.exam.presentation.api.internal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.ne.kuma.exam.common.annotation.UnitTest;
import jp.ne.kuma.exam.common.bean.Pager;
import jp.ne.kuma.exam.common.bean.UserInfo;
import jp.ne.kuma.exam.common.enumerator.QuestionMode;
import jp.ne.kuma.exam.common.filter.MockMvcRequestHeaderFilter;
import jp.ne.kuma.exam.common.util.TestUtil;
import jp.ne.kuma.exam.service.HistoryService;
import jp.ne.kuma.exam.service.dto.HistoryItem;

/**
 * 履歴ページ REST コントローラのユニットテスト
 *
 * @author Mike
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@UnitTest
public class HistoryPageRestControllerTest {

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
  private HistoryPageRestController controller;
  /** 履歴関連サービス */
  @MockBean
  private HistoryService service;
  /** ログインユーザー情報 */
  @MockBean
  private UserInfo userInfo;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .addFilters(context.getBean(SessionRepositoryFilter.class))
        .apply(springSecurity()).build();
  }

  @Test
  @WithMockUser
  public void 一覧用ページ取得_管理者の場合() throws Exception {

    // 想定値設定
    MultiValueMap<String, String> params = TestUtil.excel.loadAsRequestParam(StringUtils.LF, this.getClass(),
        "RequestParam", "Data1");
    final int page = Integer.parseInt(params.getFirst("page"));
    final int examineeId = Integer.parseInt(params.getFirst("examineeId"));
    final QuestionMode questionMode = QuestionMode.asText(params.getFirst("questionMode"));
    final boolean admin = true;
    final Pageable pageable = PageRequest.of(page, 10, Direction.DESC, "t1.created_at");
    List<HistoryItem> content = TestUtil.excel.loadAsPojoList(HistoryItem.class, this.getClass(),
        "HistoryItem", "Data1");
    Page<HistoryItem> expected = Pager.of(content).pageable(pageable).totalElements(2).build();

    // モック設定
    doReturn(admin).when(userInfo).isAdmin();
    doReturn(expected).when(service).getPage(anyInt(), any(Pageable.class), any(QuestionMode.class));

    // 試験実行
    mockMvc.perform(get("/api/internal/history")
        .params(params)
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(userInfo, times(1)).isAdmin();
    verify(service, times(1)).getPage(examineeId, pageable, questionMode);
  }

  @Test
  @WithMockUser
  public void 一覧用ページ取得_管理者ではない場合() throws Exception {

    // 想定値設定
    MultiValueMap<String, String> params = TestUtil.excel.loadAsRequestParam(StringUtils.LF, this.getClass(),
        "RequestParam", "Data1");
    final int page = Integer.parseInt(params.getFirst("page"));
    final int examineeId = 3;
    final QuestionMode questionMode = QuestionMode.asText(params.getFirst("questionMode"));
    final boolean admin = false;
    final Pageable pageable = PageRequest.of(page, 10, Direction.DESC, "t1.created_at");
    List<HistoryItem> content = TestUtil.excel.loadAsPojoList(HistoryItem.class, this.getClass(),
        "HistoryItem", "Data2");
    Page<HistoryItem> expected = Pager.of(content).pageable(pageable).totalElements(2).build();

    // モック設定
    doReturn(admin).when(userInfo).isAdmin();
    doReturn(examineeId).when(userInfo).getId();
    doReturn(expected).when(service).getPage(anyInt(), any(Pageable.class), any(QuestionMode.class));

    // 試験実行
    mockMvc.perform(get("/api/internal/history")
        .params(params)
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(userInfo, times(1)).isAdmin();
    verify(service, times(1)).getPage(examineeId, pageable, questionMode);
  }

  @Test
  @WithMockUser
  public void 詳細用ページ取得() throws Exception {

    // 想定値設定
    MultiValueMap<String, String> params = TestUtil.excel.loadAsRequestParam(StringUtils.LF, this.getClass(),
        "RequestParam", "Data2");
    final int examineeId = Integer.parseInt(params.getFirst("examineeId"));
    final int examNo = Integer.parseInt(params.getFirst("examNo"));
    final int examCoverage = Integer.parseInt(params.getFirst("examCoverage"));
    final int examCount = Integer.parseInt(params.getFirst("examCount"));
    final Pageable pageable = PageRequest.of(0, 1);
    List<HistoryItem> content = TestUtil.excel.loadAsPojoList(HistoryItem.class, this.getClass(),
        "HistoryItem", "Data3");
    Page<HistoryItem> expected = Pager.of(content).pageable(pageable).totalElements(1).build();

    // モック設定
    doReturn(expected).when(service).getDetailPage(anyInt(), anyInt(), anyInt(), anyInt(), any(Pageable.class));

    // 試験実行
    mockMvc.perform(get("/api/internal/history/detail")
        .params(params)
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(service, times(1)).getDetailPage(examineeId, examNo, examCoverage, examCount, pageable);
  }

  @Test
  @WithMockUser
  public void 固定出題モードの詳細ページ取得() throws Exception {

    // 想定値設定
    MultiValueMap<String, String> params = TestUtil.excel.loadAsRequestParam(StringUtils.LF, this.getClass(),
        "RequestParam", "Data3");
    final int examineeId = Integer.parseInt(params.getFirst("examineeId"));
    final int fixedQuestionsId = Integer.parseInt(params.getFirst("fixedQuestionsId"));
    final int examCount = Integer.parseInt(params.getFirst("examCount"));
    final Pageable pageable = PageRequest.of(0, 1);
    List<HistoryItem> content = TestUtil.excel.loadAsPojoList(HistoryItem.class, this.getClass(),
        "HistoryItem", "Data4");
    Page<HistoryItem> expected = Pager.of(content).pageable(pageable).totalElements(1).build();

    // モック設定
    doReturn(expected).when(service).getFixedDetailPage(anyInt(), anyInt(), anyInt(), any(Pageable.class));

    // 試験実行
    mockMvc.perform(get("/api/internal/history/fixedDetail")
        .params(params)
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(service, times(1)).getFixedDetailPage(examineeId, fixedQuestionsId, examCount, pageable);
  }
}
