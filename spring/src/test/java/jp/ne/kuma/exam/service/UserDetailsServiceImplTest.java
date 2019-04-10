package jp.ne.kuma.exam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;

import jp.ne.kuma.exam.common.annotation.UnitTest;
import jp.ne.kuma.exam.common.bean.UserInfo;
import jp.ne.kuma.exam.common.util.TestUtil;
import jp.ne.kuma.exam.persistence.RedmineUsersDao;
import jp.ne.kuma.exam.persistence.dto.RedmineUser;

@RunWith(SpringRunner.class)
@UnitTest
public class UserDetailsServiceImplTest {

  @InjectMocks
  private UserDetailsServiceImpl service;
  @MockBean
  private RedmineUsersDao dao;
  @MockBean
  private UserInfo userInfo;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void Redmineのユーザー情報取得してハッシュ化パスワードとソルトをスペース区切りで保持() {

    // 想定値の設定
    final String username = "username@user.co.jp";
    final String password = "hashedPassword salt";
    Optional<RedmineUser> user = Optional.ofNullable(TestUtil.excel.loadAsPojo(RedmineUser.class, this.getClass(), "RedmineUser", "Data1"));
    final User expected = new User(username, password, true, true, true, true, AuthorityUtils.NO_AUTHORITIES);

    // モックの設定
    doReturn(user).when(dao).selectUser(any(String.class));
    doNothing().when(userInfo).setId(anyInt());
    doNothing().when(userInfo).setAdmin(anyBoolean());

    // 試験実行
    final UserDetails actual = service.loadUserByUsername(username);

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(dao, times(1)).selectUser(username);
    verify(userInfo, times(1)).setId(user.get().getId());
    verify(userInfo, times(1)).setAdmin(user.get().isAdmin());
  }

  @Test
  public void Redmineのユーザー情報が見つからなかったら例外をスロー() {

    // 想定値の設定
    final String username = "username@user.co.jp";
    Optional<RedmineUser> user = Optional.empty();
    final UsernameNotFoundException expected = new UsernameNotFoundException("そんなユーザーいません。");

    // モックの設定
    doReturn(user).when(dao).selectUser(any(String.class));

    // 試験実行
    try {
      service.loadUserByUsername(username);
      fail("例外がスローされなかった");
    } catch (Exception actual) {

      // 値の検証
      assertThat(actual).isEqualToComparingFieldByFieldRecursively(expected);

      // 呼び出しの検証
      verify(dao, times(1)).selectUser(username);
    }
  }
}
