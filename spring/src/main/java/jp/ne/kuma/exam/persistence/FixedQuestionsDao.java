package jp.ne.kuma.exam.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.Update;
import org.seasar.doma.boot.ConfigAutowireable;

import jp.ne.kuma.exam.persistence.dto.FixedQuestion;

/**
 * 固定出題テーブル DAO
 *
 * @author Mike
 */
@Dao
@ConfigAutowireable
public interface FixedQuestionsDao {

  /**
   * プライマリキーで一意の固定出題データを取得。<br />
   * 第三引数が null 出ない場合、楽観排他用のデータ取得として機能する。
   *
   * @param id
   *          固定出題 ID
   * @param modifiedAt
   *          更新日時
   * @return 固定出題テーブルエンティティの Optional
   */
  @Select
  Optional<FixedQuestion> selectOne(Integer id, LocalDateTime modifiedAt);

  /**
   * 廃止フラグが設定されていないすべての固定出題データのリストを返却。
   *
   * @param examNo
   *          試験種別
   * @return 固定出題テーブルエンティティのリスト
   */
  @Select
  List<FixedQuestion> selectAll();

  /**
   * 最大の固定出題 ID を取得。
   *
   * @return 固定出題 ID
   */
  @Select
  Optional<Integer> selectMaxId();

  /**
   * 固定出題データを追加する。
   *
   * @param fixedQuestion
   *          固定出題テーブルエンティティ
   * @return 追加数
   */
  @Insert(excludeNull = true)
  int insertFixedQuestion(FixedQuestion fixedQuestion);

  /**
   * 固定出題データを更新する。
   *
   * @param fixedQuestion
   *          固定出題テーブルエンティティ
   * @return 更新数
   */
  @Update(excludeNull = true)
  int updateFixedQuestion(FixedQuestion fixedQuestion);
}
