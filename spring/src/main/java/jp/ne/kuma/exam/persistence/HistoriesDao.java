package jp.ne.kuma.exam.persistence;

import java.util.List;
import java.util.Optional;

import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.Update;
import org.seasar.doma.boot.ConfigAutowireable;
import org.springframework.data.domain.Pageable;

import jp.ne.kuma.exam.persistence.dto.Examinee;
import jp.ne.kuma.exam.persistence.dto.History;
import jp.ne.kuma.exam.persistence.dto.HistoryDto;
import jp.ne.kuma.exam.persistence.dto.HistoryPageDto;
import jp.ne.kuma.exam.service.dto.HistoryPageSelectOptions;

/**
 * 履歴テーブル DAO
 *
 * @author Mike
 */
@Dao
@ConfigAutowireable
public interface HistoriesDao {

  /**
   * 受験者 ID、試験種別、試験範囲で絞り込んだ中で、受験回数が最大の履歴データを取得する。
   *
   * @param examineeId
   *          受験者 ID
   * @param examNo
   *          試験種別
   * @param examCoverage
   *          試験範囲
   * @return 履歴テーブルエンティティの Optional
   */
  @Select
  Optional<History> selectOneByPrimaryAndMax(Integer examineeId, Integer examNo, Integer examCoverage);

  /**
   * 履歴データを更新する。
   *
   * @param history
   *          履歴テーブルエンティティ
   * @return 更新数
   */
  @Update
  int updateUserHistory(History history);

  /**
   * 履歴データを追加する。
   *
   * @param history
   *          履歴テーブルエンティティ
   * @return 追加数
   */
  @Insert
  int insertHistory(History history);

  @Select
  List<Examinee> selectAllExaminees();

  /**
   * 受験者 ID で絞り込んだリストを返却する。
   *
   * @param examineeId
   *          受験者 ID
   * @return 履歴テーブルエンティティのリスト
   */
  @Select
  List<HistoryDto> getHistoriesByExamineeId(Integer examineeId);

  /**
   * 履歴一覧画面での 1 ページ分のデータを取得。
   *
   * @param options
   *          検索条件
   * @param pageable
   *          ページング情報
   * @return 履歴テーブルエンティティのリスト
   */
  @Select
  List<HistoryPageDto> selectHistoryPage(HistoryPageSelectOptions options, Pageable pageable);
}
