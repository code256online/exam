package jp.ne.kuma.exam.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.Update;
import org.seasar.doma.boot.ConfigAutowireable;

import jp.ne.kuma.exam.persistence.dto.ExamCoverage;

/**
 * 試験範囲テーブル DAO
 *
 * @author Mike
 *
 */
@Dao
@ConfigAutowireable
public interface ExamCoveragesDao {

  /**
   * プライマリキーで一意の試験範囲データを取得する。<br />
   * 第三引数が null でない場合、楽観排他用のデータ取得として機能する。
   *
   * @param examNo
   *          試験種別
   * @param examCoverage
   *          試験範囲
   * @param modifiedAt
   *          更新日時
   * @return 試験範囲テーブルエンティティの Optional
   */
  @Select
  Optional<ExamCoverage> selectOne(int examNo, int examCoverage, LocalDateTime modifiedAt);

  /**
   * 試験種別に対応する全ての試験範囲データを取得する。
   *
   * @param examNo
   *          試験種別
   * @param includeDeleted
   *          廃止フラグ設定済みを含むかどうか
   * @return 試験範囲テーブルエンティティのリスト
   */
  @Select
  List<ExamCoverage> selectCoverageNamesByExamNo(int examNo, boolean includeDeleted);

  /**
   * 試験種別と試験範囲で一意の試験範囲データを取得する。
   *
   * @param examNo
   *          試験種別
   * @param examCoverage
   *          試験範囲
   * @return 試験範囲テーブルエンティティの Optional
   */
  @Select
  Optional<ExamCoverage> selectExactExamCoverage(int examNo, int examCoverage);

  /**
   * 対象の試験種別で登録されている試験範囲のうち最大の試験範囲 ID を取得する。
   *
   * @param examNo
   *          試験種別
   * @return 試験範囲 ID
   */
  @Select
  Optional<Integer> selectMaxId(int examNo);

  /**
   * 試験範囲データを新規登録する。
   *
   * @param coverage
   *          試験範囲テーブルエンティティ
   * @return 登録数
   */
  @Insert(excludeNull = true)
  int insertExamCoverage(ExamCoverage coverage);

  /**
   * 試験範囲データを更新する。
   *
   * @param coverage
   *          試験範囲テーブルエンティティ
   * @return 更新数
   */
  @Update(excludeNull = true)
  int updateExamCoverage(ExamCoverage coverage);
}
