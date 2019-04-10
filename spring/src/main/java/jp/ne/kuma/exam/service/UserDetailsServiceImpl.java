package jp.ne.kuma.exam.service;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.ne.kuma.exam.common.bean.UserInfo;
import jp.ne.kuma.exam.persistence.RedmineUsersDao;
import jp.ne.kuma.exam.persistence.dto.RedmineUser;

/**
 * 認証用にユーザー情報を取得するサービス
 *
 * @author Mike
 *
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  /** Redmine の ユーザー情報テーブル DAO */
  @Autowired
  private RedmineUsersDao dao;
  /** セッションに持つユーザー情報 */
  @Autowired
  private UserInfo userInfo;

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    // SQL 実行
    Optional<RedmineUser> result = dao.selectUser(username);
    if (!result.isPresent()) {
      // 入力された ID でユーザーが見つからなかったら例外をスロー
      throw new UsernameNotFoundException("そんなユーザーいません。");
    }

    // 取得したユーザーの ID と管理者フラグをセッションに持っておく
    RedmineUser user = result.get();
    userInfo.setId(user.getId());
    userInfo.setAdmin(user.isAdmin());

    // パスワードの一致確認のための前処理
    String pass = String.join(StringUtils.SPACE, user.getHashedPassword(), user.getSalt());
    return new User(user.getLogin(), pass, true, true, true, true, AuthorityUtils.NO_AUTHORITIES);
  }
}
