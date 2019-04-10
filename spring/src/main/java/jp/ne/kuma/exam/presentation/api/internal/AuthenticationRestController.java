package jp.ne.kuma.exam.presentation.api.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.ne.kuma.exam.common.bean.UserInfo;
import jp.ne.kuma.exam.common.util.PropertiesUtil;
import jp.ne.kuma.exam.presentation.form.LoginForm;

/**
 * 認証関連 REST コントローラ
 *
 * @author Mike
 */
@RestController
@RequestMapping("/api/internal/auth")
public class AuthenticationRestController {

  /** 認証プロバイダ */
  @Autowired
  private AuthenticationProvider authProvider;
  /** ユーザー情報 */
  @Autowired
  private UserInfo userInfo;
  /** プロパティコピーユーティリティ */
  @Autowired
  private PropertiesUtil propertiesUtil;

  /**
   * 現在の認証状況を取得
   *
   * @return 認証されていれば認証済みユーザー
   */
  @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public UserInfo user() {
    return propertiesUtil.copyProperties(UserInfo.class, userInfo);
  }

  /**
   * ログイン
   *
   * @param form
   *          認証情報
   * @return 成功すれば true
   */
  @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public boolean login(@RequestBody LoginForm form) {

    Authentication auth = new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword());
    try {

      Authentication result = authProvider.authenticate(auth);
      SecurityContextHolder.getContext().setAuthentication(result);

      return true;

    } catch (AuthenticationException e) {

      // 認証失敗時は、セッションのユーザー情報をクリアしておく
      userInfo.clear();
      // そのままスロー
      throw e;
    }
  }

  /**
   * ログアウト
   */
  @PostMapping(value = "/logout")
  public void logout() {

    SecurityContextHolder.clearContext();
    userInfo.clear();
  }
}
