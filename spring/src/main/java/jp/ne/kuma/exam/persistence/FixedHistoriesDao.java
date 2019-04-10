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
import jp.ne.kuma.exam.persistence.dto.FixedHistory;
import jp.ne.kuma.exam.persistence.dto.FixedHistoryDto;
import jp.ne.kuma.exam.persistence.dto.FixedHistoryPageDto;
import jp.ne.kuma.exam.service.dto.FixedHistoryPageSelectOptions;

/**
 * 固定出題履歴テーブル DAO
 *
 * @author Mike
 */
@Dao
@ConfigAutowireable
public interface FixedHistoriesDao {

  /**
   * 受験者 ID、固定出題 ID で絞り込んだ中で受験回数が最大のデータを取得する。
   *
   * @param examineeId
   *          受験者 ID
   * @param fixedQuestionsId
   *          固定出題 ID
   * @return 固定出題履歴テーブルエンティティの Optional
   */
  @Select
  Optional<FixedHistory> selectOneByPrimaryAndMax(Integer examineeId,Integer fixedQuestionsId);

  /**
   * 固定出題履歴データを更新する
   *
   * @param history
   *          固定出題履歴テーブルエンティティ
   * @return 更新数
   */
  @Update
  int updateUserHistory(FixedHistory history);

  /**
   * 固定出題履歴データを追加する
   *
   * @param history
   *          固定出題履歴テーブルエンティティ
   * @return 追加数
   */
  @Insert
  int insertHistory(FixedHistory history);

  @Select
  List<Examinee> selectAllExaminees();

  /**
   * 受験者 ID で絞り込んだリストを返却する
   *
   * @param examineeId
   *          受験者 ID
   * @return 固定出題履歴テーブルエンティティのリスト
   */
  @Select
  List<FixedHistoryDto> getHistoriesByExamineeId(Integer examineeId);

  /**
   * 履歴一覧画面で表示するデータを取得する
   *
   * @param options
   *          検索条件
   * @param pageable
   *          ページング情報
   * @return 固定出題履歴テーブルエンティティのリスト
   */
  @Select
  List<FixedHistoryPageDto> selectHistoryPage(FixedHistoryPageSelectOptions options, Pageable pageable);
}
