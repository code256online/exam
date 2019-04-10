package jp.ne.kuma.exam.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.Update;
import org.seasar.doma.boot.ConfigAutowireable;
import org.springframework.data.domain.Pageable;

import jp.ne.kuma.exam.persistence.dto.Answer;
import jp.ne.kuma.exam.persistence.dto.AnswerPageDto;
import jp.ne.kuma.exam.service.dto.QuestionData;

/**
 * 試験の答えテーブル DAO
 *
 * @author Mike
 *
 */
@Dao
@ConfigAutowireable
public interface AnswersDao {

  /**
   * プライマリキーで一意に定まる出題データを取得
   *
   * @param examNo
   *          試験種別
   * @param questionNo
   *          問題番号
   * @return 出題データエンティティ
   */
  @Select
  Answer selectOneByPrimary(Integer examNo, Integer questionNo);

  /**
   * 楽観排他確認用
   *
   * @param examNo
   *          試験種別
   * @param questionNo
   *          問題番号
   * @param modifiedAt
   *          更新日時
   * @return 出題データエンティティの Optional
   */
  @Select
  Optional<Answer> selectOne(Integer examNo, Integer questionNo, LocalDateTime modifiedAt);

  /**
   * 試験種別と試験範囲で絞り込んだ件数のみ返す
   *
   * @param examNo
   *          試験種別
   * @param examCoverage
   *          試験範囲
   * @return ヒット件数
   */
  @Select
  Integer selectQuestionCount(Integer examNo, Integer examCoverage);

  /**
   * 試験種別で絞り込んだ件数の見返す
   *
   * @param examNo
   *          試験種別
   * @return ヒット件数
   */
  @Select
  long selectCountByExamNo(Integer examNo);

  /**
   * 対象試験種別で登録されているデータのうち、最大の問題番号を返す
   *
   * @param examNo
   *          試験種別
   * @return 問題番号
   */
  @Select
  int selectMaxQuestionNo(Integer examNo);

  /**
   * 出題データメンテナンスの一覧画面用にデータを取得
   *
   * @param examNo
   *          試験種別
   * @param pageable
   *          ページング情報
   * @return 1 ページ分の情報
   */
  @Select
  List<AnswerPageDto> selectPage(Integer examNo, Pageable pageable);

  /**
   * 出題画面用にデータを取得
   *
   * @param examNo
   *          試験種別
   * @param examCoverage
   *          試験範囲
   * @param offset
   *          オフセット
   * @return 出題データエンティティ
   */
  @Select
  Answer selectPageByExamCoverage(Integer examNo, Integer examCoverage, Integer offset);

  /**
   * 試験種別と問題番号リストから、問題番号ごとの正答のリストを返す。
   *
   * @param examNo
   *          試験種別
   * @param incorrects
   *          問題番号リスト
   * @return 正答リスト
   */
  @Select
  List<String> selectCorrectAnswers(List<QuestionData> incorrects);

  @Select
  int countRegistered(List<QuestionData> data);

  /**
   * 試験の答えテーブルにデータを新規登録する。
   *
   * @param answer
   *          試験の答えテーブルエンティティ
   * @return 追加した件数
   */
  @Insert(excludeNull = true)
  int insertAnswer(Answer answer);

  /**
   * 試験の答えテーブルのデータを更新する。
   *
   * @param answer
   *          試験の答えテーブルエンティティ
   * @return 更新した件数
   */
  @Update(excludeNull = true)
  int updateAnswer(Answer answer);
}
