package jp.ne.kuma.exam.presentation.api.internal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.ne.kuma.exam.common.annotation.UnitTest;
import jp.ne.kuma.exam.common.bean.UserInfo;
import jp.ne.kuma.exam.common.filter.MockMvcRequestHeaderFilter;
import jp.ne.kuma.exam.common.util.PropertiesUtil;
import jp.ne.kuma.exam.common.util.TestUtil;
import jp.ne.kuma.exam.presentation.form.LoginForm;

/**
 * 認証関連 REST コントローラのユニットテスト
 *
 * @author Mike
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@UnitTest
public class AuthenticationRestControllerTest {

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
  private AuthenticationRestController controller;
  /** 認証プロバイダ */
  @MockBean
  private AuthenticationProvider authProvider;
  /** ユーザー情報 */
  @MockBean
  private UserInfo userInfo;
  /** プロパティコピーユーティリティ */
  @SpyBean
  private PropertiesUtil propertiesUtil;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .addFilters(context.getBean(SessionRepositoryFilter.class))
        .apply(springSecurity()).build();
  }

  @Test
  @WithMockUser
  public void 現在の認証状況を取得() throws Exception {

    // 想定値設定
    final UserInfo expected = TestUtil.excel.loadAsPojo(UserInfo.class, this.getClass(), "UserInfo", "Data1");

    // モック設定
    doReturn(expected).when(propertiesUtil).copyProperties(any(), any(UserInfo.class));

    // 試験実行
    mockMvc.perform(get("/api/internal/auth/user")
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));

    // 検証
    verify(propertiesUtil, times(1)).copyProperties(UserInfo.class, userInfo);
  }

  @Test
  @WithAnonymousUser
  public void ログイン() throws Exception {

    // 想定値設定
    final LoginForm form = TestUtil.excel.loadAsPojo(LoginForm.class, this.getClass(), "LoginForm", "Data1");
    final Authentication auth = new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword());
    final boolean expected = true;

    // モック設定
    doReturn(auth).when(authProvider).authenticate(auth);

    // 試験実行
    mockMvc.perform(post("/api/internal/auth/login")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string(String.valueOf(expected)));

    // 検証
    verify(authProvider, times(1)).authenticate(auth);
  }

  @Test
  @WithAnonymousUser
  public void ログイン時に認証エラー() throws Exception {

    // 想定値設定
    final LoginForm form = TestUtil.excel.loadAsPojo(LoginForm.class, this.getClass(), "LoginForm", "Data1");
    final Authentication auth = new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword());
    final AuthenticationException exception = new AuthenticationCredentialsNotFoundException("エラーメッセージ");

    // モック設定
    doThrow(exception).when(authProvider).authenticate(auth);
    doNothing().when(userInfo).clear();

    // 試験実行
    mockMvc.perform(post("/api/internal/auth/login")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(form))
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(content().json(mapper.writeValueAsString(exception)));

    // 検証
    verify(authProvider, times(1)).authenticate(auth);
    verify(userInfo, times(1)).clear();
  }

  @Test
  @WithMockUser
  public void ログアウト() throws Exception {

    // モック設定
    doNothing().when(userInfo).clear();

    // 試験実行
    mockMvc.perform(post("/api/internal/auth/logout")
        .with(new MockMvcRequestHeaderFilter(context)))
        .andDo(print())
        .andExpect(status().isOk());

    // 検証
    verify(userInfo, times(1)).clear();
  }
}
