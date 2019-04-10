package jp.ne.kuma.exam.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.ne.kuma.exam.common.bean.Pager;
import jp.ne.kuma.exam.common.bean.QAHistory;
import jp.ne.kuma.exam.common.enumerator.AnswerStates;
import jp.ne.kuma.exam.common.enumerator.QuestionMode;
import jp.ne.kuma.exam.common.util.PropertiesUtil;
import jp.ne.kuma.exam.persistence.AnswersDao;
import jp.ne.kuma.exam.persistence.ExamsDao;
import jp.ne.kuma.exam.persistence.FixedHistoriesDao;
import jp.ne.kuma.exam.persistence.HistoriesDao;
import jp.ne.kuma.exam.persistence.dto.Exam;
import jp.ne.kuma.exam.persistence.dto.Examinee;
import jp.ne.kuma.exam.persistence.dto.FixedHistory;
import jp.ne.kuma.exam.persistence.dto.FixedHistoryPageDto;
import jp.ne.kuma.exam.persistence.dto.History;
import jp.ne.kuma.exam.persistence.dto.HistoryPageDto;
import jp.ne.kuma.exam.service.dto.FixedHistoryPageSelectOptions;
import jp.ne.kuma.exam.service.dto.HistoryItem;
import jp.ne.kuma.exam.service.dto.HistoryPageSelectOptions;
import jp.ne.kuma.exam.service.dto.QuestionData;

/**
 * 履歴の作成・ページング取得サービス
 *
 * @author Mike
 */
@Service
public class HistoryService {

  /** プロパティコピーユーティリティ */
  @Autowired
  PropertiesUtil propertiesUtil;
  /** 履歴テーブル DAO */
  @Autowired
  private HistoriesDao historiesDao;
  /** 固定出題履歴テーブル DAO */
  @Autowired
  private FixedHistoriesDao fixedHistoriesDao;
  /** 試験の答えテーブル DAO */
  @Autowired
  private AnswersDao answersDao;
  /** 試験種別テーブル DAO */
  @Autowired
  private ExamsDao examsDao;
  /** 時刻オブジェクト */
  @Autowired
  private Clock clock;

  /**
   * 履歴を作成。
   *
   * @param qaHistory
   *          このセッションの回答データ
   */
  @Transactional(rollbackFor = Exception.class)
  public void update(QAHistory qaHistory) {

    // 固定出題モードなら固定出題の作成メソッドへ
    if (qaHistory.getQuestionMode() == QuestionMode.FIXED) {
      updateFixed(qaHistory);
      return;
    }

    LocalDateTime dateTime = LocalDateTime.now(clock);
    History history = propertiesUtil.copyProperties(History.class, qaHistory);
    history.setDeleted(false);
    history.setCreatedAt(dateTime);
    history.setModifiedAt(dateTime);

    // 回答数を算出。
    history.setAnswerCount((int) qaHistory.getAnswerStates().stream()
        .filter(x -> x.getState() != AnswerStates.NOT_ANSWERED).count());
    // 正答数を算出。
    history.setCorrectCount((int) qaHistory.getAnswerStates().stream()
        .filter(x -> x.getState() == AnswerStates.CORRECT).count());

    // 同一種別・同一範囲の受験回数（いったん1を入れておく）
    history.setExamCount(1);
    Optional<History> maxCount = historiesDao.selectOneByPrimaryAndMax(
        qaHistory.getExamineeId(),
        qaHistory.getExamNo(),
        qaHistory.getExamCoverage());

    // 既に同一種別・同一範囲での受験履歴が存在する場合は、その最大値に +1 した値を受験回数に設定する
    if (maxCount.isPresent()) {
      history.setExamCount(maxCount.get().getExamCount() + 1);
    }

    if (history.getQuestionCount() != history.getCorrectCount()) {
      // 全問正解でないなら、不正解だった問題番号をカンマ区切りにして登録。
      history.setIncorrectQuestions(qaHistory.getAnswerStates().stream()
          .filter(x -> x.getState() != AnswerStates.CORRECT)
          .map(x -> x.getQuestionNo().toString())
          .collect(Collectors.joining(",")));
    }

    // ここで SQL 実行。
    historiesDao.insertHistory(history);
  }

  /**
   * 固定出題の履歴を作成。
   *
   * @param qaHistory
   *          このセッションの回答データ
   */
  private void updateFixed(QAHistory qaHistory) {

    LocalDateTime dateTime = LocalDateTime.now(clock);
    FixedHistory history = propertiesUtil.copyProperties(FixedHistory.class, qaHistory);
    history.setDeleted(false);
    history.setCreatedAt(dateTime);
    history.setModifiedAt(dateTime);

    // 回答数を算出。
    history.setAnswerCount((int) qaHistory.getAnswerStates().stream()
        .filter(x -> x.getState() != AnswerStates.NOT_ANSWERED).count());
    // 正答数を算出。
    history.setCorrectCount((int) qaHistory.getAnswerStates().stream()
        .filter(x -> x.getState() == AnswerStates.CORRECT).count());

    // 同一固定出題の受験回数（いったん1を入れておく）
    history.setExamCount(1);
    Optional<FixedHistory> maxCount = fixedHistoriesDao.selectOneByPrimaryAndMax(
        qaHistory.getExamineeId(),
        qaHistory.getFixedQuestionsId());

    // 既に同一試験種別・同一固定出題範囲での受験履歴が存在するなら、その最大値に +1 した値を受験回数に設定
    if (maxCount.isPresent()) {
      history.setExamCount(maxCount.get().getExamCount() + 1);
    }

    if (history.getQuestionCount() != history.getCorrectCount()) {
      // 全問正解でないなら、不正解だった問題番号を「試験番号-問題番号」のカンマ区切りで設定。
      history.setIncorrectQuestions(qaHistory.getAnswerStates().stream()
          .filter(x -> x.getState() != AnswerStates.CORRECT)
          .map(x -> String.join("-", x.getExamNo().toString(), x.getQuestionNo().toString()))
          .collect(Collectors.joining(",")));
    }

    // ここで SQL 実行
    fixedHistoriesDao.insertHistory(history);
  }

  public List<Examinee> getExaminees(QuestionMode mode) {
    if (mode == QuestionMode.FIXED) {
      return fixedHistoriesDao.selectAllExaminees();
    } else {
      return historiesDao.selectAllExaminees();
    }
  }

  /**
   * 履歴一覧のページング取得を行う。
   *
   * @param examineeId
   *          受験者 ID
   * @param pageable
   *          ページング情報
   * @param questionMode
   *          出題モード
   * @return 履歴の 1 ページ表示分のデータ
   */
  @Transactional(readOnly = true)
  public Page<HistoryItem> getPage(Integer examineeId, Pageable pageable, QuestionMode questionMode) {

    // 固定出題モードの場合は固定出題モードの履歴ページ取得メソッドへ
    if (questionMode == QuestionMode.FIXED) {
      return getFixedPage(examineeId, pageable);
    }

    // 検索条件の設定。
    HistoryPageSelectOptions options = new HistoryPageSelectOptions();
    options.setExamineeId(examineeId);
    // ここで SQL 実行。これが 1 ページ分のデータのリスト
    List<HistoryPageDto> result = historiesDao.selectHistoryPage(options, pageable);
    // 画面表示用にマッピング
    List<HistoryItem> items = result.stream().map(historyMapper()).collect(Collectors.toList());
    // ページングせずに全件カウントしたら何件あるのか（全ページ数の算出に必要）
    long totalElements = result.isEmpty() ? 0 : result.get(0).getCount();

    return Pager.of(items).pageable(pageable).totalElements(totalElements).build();
  }

  /**
   * 固定出題の履歴一覧ページ情報取得。
   *
   * @param examineeId
   *          受験者 ID
   * @param pageable
   *          ページング情報
   * @return 固定出題の履歴の 1 ページ表示分のデータ
   */
  @Transactional(readOnly = true)
  private Page<HistoryItem> getFixedPage(Integer examineeId, Pageable pageable) {

    // 検索条件設定。
    FixedHistoryPageSelectOptions options = new FixedHistoryPageSelectOptions();
    options.setExamineeId(examineeId);
    // ここで SQL 実行。これが 1 ページ分のデータのリスト
    List<FixedHistoryPageDto> result = fixedHistoriesDao.selectHistoryPage(options, pageable);
    // 画面表示用にマッピング
    List<Exam> exams = examsDao.selectAllExamNames();
    List<HistoryItem> items = result.stream().map(fixedHistoryMapper(exams)).collect(Collectors.toList());
    // ページングせずに全件カウントしたら何件あるのか（全ページ数の算出に必要）
    long totalElements = result.isEmpty() ? 0 : result.get(0).getCount();

    return Pager.of(items).pageable(pageable).totalElements(totalElements).build();
  }

  /**
   * 履歴詳細画面の表示データ取得
   *
   * @param examineeId
   *          受験者 ID
   * @param examNo
   *          試験番号
   * @param examCoverage
   *          試験範囲 ID
   * @param examCount
   *          受験回数
   * @param pageable
   *          ページング情報
   * @return 履歴詳細画面の表示データ
   */
  @Transactional(readOnly = true)
  public Page<HistoryItem> getDetailPage(Integer examineeId, Integer examNo, Integer examCoverage, Integer examCount,
      Pageable pageable) {

    // 検索条件の設定。
    HistoryPageSelectOptions options = new HistoryPageSelectOptions();
    options.setExamineeId(examineeId);
    options.setExamNo(examNo);
    options.setExamCoverage(examCoverage);
    options.setExamCount(examCount);

    // ここで SQL 実行。これが詳細画面で表示するための、直近の受験履歴を含む全データ。
    List<HistoryPageDto> result = historiesDao.selectHistoryPage(options, pageable);
    // 画面表示用にマッピング。
    List<HistoryItem> items = result.stream().map(historyMapper()).collect(Collectors.toList());
    // これは...なんで計算してるのか覚えてない。いらないかも。
    long totalElements = result.isEmpty() ? 0 : result.get(0).getCount();

    if (CollectionUtils.isNotEmpty(items.get(0).getIncorrectQuestions())) {
      // リストの先頭（詳細表示する履歴データ）に不正解だった問題が存在すれば、SQL を実行して
      // 該当する問題の正答の情報を出題データテーブルから取得。
      items.get(0).setCorrectAnswers(answersDao.selectCorrectAnswers(items.get(0).getIncorrectQuestions()));
    }

    return Pager.of(items).pageable(pageable).totalElements(totalElements).build();
  }

  /**
   * 固定出題の履歴詳細画面の表示用データ取得。
   *
   * @param examineeId
   *          受験者 ID
   * @param fixedQuestionsId
   *          固定出題 ID
   * @param examCount
   *          受験回数
   * @param pageable
   *          ページング情報
   * @return 固定出題の履歴詳細画面表示用データ
   */
  @Transactional(readOnly = true)
  public Page<HistoryItem> getFixedDetailPage(Integer examineeId, Integer fixedQuestionsId, Integer examCount,
      Pageable pageable) {

    // 検索条件を設定。
    FixedHistoryPageSelectOptions options = new FixedHistoryPageSelectOptions();
    options.setExamineeId(examineeId);
    options.setFixedQuestionsId(fixedQuestionsId);
    options.setExamCount(examCount);

    // SQL を実行して、直近の受験履歴を含む表示用の全データ取得。
    List<FixedHistoryPageDto> result = fixedHistoriesDao.selectHistoryPage(options, pageable);
    // 画面表示用にマッピング
    List<Exam> exams = examsDao.selectAllExamNames();
    List<HistoryItem> items = result.stream().map(fixedHistoryMapper(exams)).collect(Collectors.toList());
    // これは...なんで計算してるのか覚えてない。いらないかも。
    long totalElements = result.isEmpty() ? 0 : result.get(0).getCount();

    if (CollectionUtils.isNotEmpty(items.get(0).getIncorrectQuestions())) {
      // リストの先頭（詳細表示する履歴データ）に不正解だった問題が存在すれば、SQL を実行して
      // 該当する問題の正答の情報を出題データテーブルから取得。
      items.get(0).setCorrectAnswers(answersDao.selectCorrectAnswers(items.get(0).getIncorrectQuestions()));
    }

    return Pager.of(items).pageable(pageable).totalElements(totalElements).build();
  }

  /**
   * 回答終了画面で出力する履歴詳細画面のデータ取得。
   *
   * @param examineeId
   *          受験者 ID
   * @param pageable
   *          ページング情報
   * @param questionMode
   *          出題モード
   * @return 回答終了画面の表示用データ
   */
  @Transactional(readOnly = true)
  public Page<HistoryItem> getFinishPage(Integer examineeId, Pageable pageable, QuestionMode questionMode) {

    // 履歴一覧ページ取得メソッドを呼んで結果を保持
    Page<HistoryItem> ret = getPage(examineeId, pageable, questionMode);
    if (CollectionUtils.isNotEmpty(ret.getContent().get(0).getIncorrectQuestions())) {
      // リストの先頭（詳細表示する履歴データ）に不正解だった問題が存在すれば、SQL を実行して
      // 該当する問題の正答の情報を出題データテーブルから取得。
      ret.getContent().get(0).setCorrectAnswers(
          answersDao.selectCorrectAnswers(ret.getContent().get(0).getIncorrectQuestions()));
    }

    return ret;
  }

  /**
   * 履歴データの画面表示用マッピング
   *
   * @return マッピング用関数
   */
  private Function<HistoryPageDto, HistoryItem> historyMapper() {

    return x -> {

      HistoryItem ret = propertiesUtil.copyProperties(HistoryItem.class, x);
      if (x.getStartDatetime() != null) {
        // 回答開始時間が記録されている場合は、回答終了までにかかった時間を計算。
        Duration min = Duration.between(x.getStartDatetime(), x.getTimestamp());
        Duration sec = Duration.between(x.getStartDatetime(), x.getTimestamp())
            .minus(Duration.ofMinutes(min.toMinutes()));
        ret.setDurationMinutes(min.toMinutes());
        ret.setDurationSeconds(sec.toMillis() / 1000);
      }

      if (StringUtils.isNotBlank(x.getIncorrectQuestions())) {
        // 不正解の問題番号はカンマ区切り文字列で取れるが、リスト形式で画面に渡したいので分割してリストに変換。
        ret.setIncorrectQuestions(Stream.of(StringUtils.split(x.getIncorrectQuestions(), ","))
            .map(y -> {
              QuestionData data = new QuestionData();
              data.setExamNo(x.getExamNo());
              data.setExamName(x.getExamName());
              data.setQuestionNo(Integer.parseInt(y));
              return data;
            }).collect(Collectors.toList()));
      } else {
        // 全問正解なら空のリストを入れておく
        ret.setIncorrectQuestions(new ArrayList<>());
      }

      return ret;
    };
  }

  /**
   * 固定出題の履歴データの画面表示用マッピング
   *
   * @return マッピング用関数
   */
  private Function<FixedHistoryPageDto, HistoryItem> fixedHistoryMapper(List<Exam> exams) {

    return x -> {

      HistoryItem ret = propertiesUtil.copyProperties(HistoryItem.class, x);
      if (x.getStartDatetime() != null) {
        // 回答開始時間が記録されている場合は、回答終了までにかかった時間を計算。
        Duration min = Duration.between(x.getStartDatetime(), x.getTimestamp());
        Duration sec = Duration.between(x.getStartDatetime(), x.getTimestamp())
            .minus(Duration.ofMinutes(min.toMinutes()));
        ret.setDurationMinutes(min.toMinutes());
        ret.setDurationSeconds(sec.toMillis() / 1000);
      }

      if (StringUtils.isNotBlank(x.getIncorrectQuestions())) {
        // 不正解の問題番号はカンマ区切り文字列で取れるが、リスト形式で画面に渡したいので分割してリストに変換。
        ret.setIncorrectQuestions(Stream.of(StringUtils.split(x.getIncorrectQuestions(), ","))
            .map(y -> {
              String[] array = StringUtils.split(y, "-");
              QuestionData data = new QuestionData();
              data.setExamNo(Integer.parseInt(array[0]));
              data.setExamName(exams.stream()
                  .filter(z -> z.getExamNo() == data.getExamNo())
                  .map(z -> z.getExamName())
                  .findAny().orElse("不明な試験種別"));
              data.setQuestionNo(Integer.parseInt(array[1]));
              return data;
            }).collect(Collectors.toList()));
      } else {
        // 全問正解なら空のリストを入れておく
        ret.setIncorrectQuestions(new ArrayList<>());
      }

      return ret;
    };
  }
}
