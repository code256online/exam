package jp.ne.kuma.exam.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.Update;
import org.seasar.doma.boot.ConfigAutowireable;

import jp.ne.kuma.exam.persistence.dto.Exam;

/**
 * 試験種別テーブル DAO
 *
 * @author Mike
 *
 */
@Dao
@ConfigAutowireable
public interface ExamsDao {

  /**
   * プライマリキーで一意の試験種別を取得する。<br />
   * 第二引数が null ではない場合、楽観排他用の検索として機能する。
   *
   * @param examNo
   *          試験種別
   * @param modifiedAt
   *          更新日時
   * @return 試験種別テーブルエンティティの Optional
   */
  @Select
  Optional<Exam> selectOne(Integer examNo, LocalDateTime modifiedAt);

  /**
   * 全ての試験種別を取得する。廃止フラグ設定済みは含まない。
   *
   * @return 試験種別テーブルエンティティのリスト
   */
  @Select
  List<Exam> selectAllExamNames();

  /**
   * 登録されている試験種別のうち、最大の試験番号を取得する。
   *
   * @return 最大の試験番号
   */
  @Select
  Optional<Integer> selectMaxId();

  /**
   * 試験種別データを新規登録する。
   *
   * @param exam
   *          試験種別テーブルエンティティ
   * @return 登録数
   */
  @Update(excludeNull = true)
  int updateExam(Exam exam);

  /**
   * 試験種別データを更新する。
   *
   * @param exam
   *          試験種別テーブルエンティティ
   * @return 更新数
   */
  @Insert(excludeNull = true)
  int insertExam(Exam exam);
}
