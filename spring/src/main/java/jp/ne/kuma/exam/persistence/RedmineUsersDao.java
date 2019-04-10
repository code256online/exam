package jp.ne.kuma.exam.persistence;

import java.util.Optional;

import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.boot.ConfigAutowireable;

import jp.ne.kuma.exam.persistence.dto.RedmineUser;

/**
 * Redmine のユーザー情報テーブル DAO
 *
 */
@Dao
@ConfigAutowireable
public interface RedmineUsersDao {

  /**
   * ログイン ID で一意のユーザーを検索する。
   *
   * @param username
   *          ログイン ID
   * @return ユーザー情報テーブルエンティティの Optional
   */
  @Select
  Optional<RedmineUser> selectUser(String username);
}
