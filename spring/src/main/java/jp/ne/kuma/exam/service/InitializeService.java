package jp.ne.kuma.exam.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.ne.kuma.exam.common.bean.Pager;
import jp.ne.kuma.exam.common.util.PropertiesUtil;
import jp.ne.kuma.exam.persistence.AnswersDao;
import jp.ne.kuma.exam.persistence.ExamCoveragesDao;
import jp.ne.kuma.exam.persistence.ExamsDao;
import jp.ne.kuma.exam.persistence.FixedQuestionsDao;
import jp.ne.kuma.exam.persistence.dto.Answer;
import jp.ne.kuma.exam.persistence.dto.AnswerPageDto;
import jp.ne.kuma.exam.persistence.dto.Exam;
import jp.ne.kuma.exam.persistence.dto.ExamCoverage;
import jp.ne.kuma.exam.persistence.dto.FixedQuestion;
import jp.ne.kuma.exam.presentation.form.FixedQuestionForm;
import jp.ne.kuma.exam.service.dto.QuestionData;

/**
 * 出題初期化関連サービス
 *
 * @author Mike
 */
@Service
public class InitializeService {

  /** 試験の答えテーブル DAO */
  @Autowired
  private AnswersDao answersDao;
  /** 試験種別テーブル DAO */
  @Autowired
  private ExamsDao examsDao;
  /** 試験範囲テーブル DAO */
  @Autowired
  private ExamCoveragesDao examCoveragesDao;
  /** 固定出題テーブル DAO */
  @Autowired
  private FixedQuestionsDao fixedQuestionsDao;
  /** プロパティコピーユーティリティ */
  @Autowired
  private PropertiesUtil propertiesUtil;

  /**
   * 廃止フラグが設定されていない全ての試験種別を取得。
   *
   * @return 全ての試験種別テーブルエンティティ
   */
  @Transactional(readOnly = true)
  public List<Exam> getAllExams() {
    return examsDao.selectAllExamNames();
  }

  /**
   * 引数の試験種別・試験範囲に紐付く廃止フラグが設定されていない出題データの数を数えて返す。
   *
   * @param examNo
   *          試験種別
   * @param examCoverage
   *          試験範囲
   * @return 存在する出題データの数
   */
  @Transactional(readOnly = true)
  public Integer getCount(Integer examNo, Integer examCoverage) {
    return answersDao.selectQuestionCount(examNo, examCoverage);
  }

  /**
   * 引数の試験種別に紐付く全ての試験範囲を取得。<br />
   * 第二引数が true の場合、廃止フラグが設定されているデータを含める。
   *
   * @param examNo
   *          試験種別
   * @param includeDeleted
   *          廃止フラグが設定されているデータを含むか
   * @return 全ての試験範囲テーブルエンティティ
   */
  @Transactional(readOnly = true)
  public List<ExamCoverage> getCoverages(int examNo, boolean includeDeleted) {
    return examCoveragesDao.selectCoverageNamesByExamNo(examNo, includeDeleted);
  }

  /**
   * 廃止フラグが設定されていない全ての固定出題データを取得。
   *
   * @return 全ての固定出題テーブルエンティティ
   */
  @Transactional(readOnly = true)
  public List<FixedQuestionForm> getFixedQuestions() {
    return fixedQuestionsDao.selectAll().stream().map(x -> {
      FixedQuestionForm ret = propertiesUtil.copyProperties(FixedQuestionForm.class, x);
      ret.setQuestions(Stream.of(StringUtils.split(x.getQuestions(), ",")).map(y -> {
        String[] array = StringUtils.split(y, "-");
        QuestionData data = new QuestionData();
        data.setExamNo(Integer.parseInt(array[0]));
        data.setQuestionNo(Integer.parseInt(array[1]));
        return data;
      }).collect(Collectors.toList()));
      return ret;
    }).collect(Collectors.toList());
  }

  /**
   * 引数の試験種別と固定出題 ID で定まる固定出題データについて、
   * 設定されている全ての問題番号をリストにして返す。
   *
   * @param fixedQuestionsId
   *          固定出題 ID
   * @return 全ての問題番号
   */
  @Transactional(readOnly = true)
  public List<QuestionData> getFixedQuestionNumbers(Integer fixedQuestionsId) {
    return Stream.of(StringUtils.split(
        fixedQuestionsDao.selectOne(fixedQuestionsId, null).get().getQuestions(), ','))
        .map(x -> StringUtils.split(x, "-"))
        .map(x -> {
          QuestionData ret = new QuestionData();
          ret.setExamNo(Integer.parseInt(x[0]));
          ret.setQuestionNo(Integer.parseInt(x[1]));
          return ret;
        }).collect(Collectors.toList());
  }

  /**
   * 引数の試験番号の試験種別データを取得。廃止フラグ設定済みを含む。
   *
   * @param examNo
   *          試験番号
   * @return 試験種別テーブルエンティティの Optional
   */
  @Transactional(readOnly = true)
  public Optional<Exam> getExactExam(int examNo) {
    return examsDao.selectOne(examNo, null);
  }

  /**
   * 引数の試験番号と試験範囲 ID で定まる試験範囲データを取得。廃止フラグ設定済みを含む。
   *
   * @param examNo
   *          試験番号
   * @param id
   *          試験範囲 ID
   * @return 試験範囲テーブルエンティティの Optional
   */
  @Transactional(readOnly = true)
  public Optional<ExamCoverage> getExactExamCoverage(int examNo, int id) {
    return examCoveragesDao.selectExactExamCoverage(examNo, id);
  }

  /**
   * 引数の ID で定まる固定出題データを取得。廃止フラグ設定済みを含む。
   *
   * @param id
   *          固定出題 ID
   * @return 固定出題テーブルエンティティの Optional
   */
  @Transactional(readOnly = true)
  public Optional<FixedQuestionForm> getExactFixedQuestion(int id) {

    Optional<FixedQuestion> result = fixedQuestionsDao.selectOne(id, null);
    List<Exam> exams = examsDao.selectAllExamNames();
    if (result.isPresent()) {

      FixedQuestionForm ret = propertiesUtil.copyProperties(FixedQuestionForm.class, result.get());
      ret.setQuestions(Stream.of(StringUtils.split(result.get().getQuestions(), ",")).map(x -> {
        String[] array = StringUtils.split(x, "-");
        QuestionData data = new QuestionData();
        data.setExamNo(Integer.parseInt(array[0]));
        data.setExamName(exams.stream().filter(y -> y.getExamNo() == data.getExamNo())
            .map(y -> y.getExamName()).findAny().orElse(null));
        data.setQuestionNo(Integer.parseInt(array[1]));
        return data;
      }).collect(Collectors.toList()));

      return Optional.ofNullable(ret);

    } else {
      return Optional.empty();
    }
  }

  /**
   * 出題データメンテナンス画面での出題データのページング表示用データ取得
   *
   * @param examNo
   *          試験種別
   * @param pageable
   *          ページング情報
   * @return 出題データの 1 ページ表示用データ
   */
  @Transactional(readOnly = true)
  public Page<AnswerPageDto> getAnswerPage(Integer examNo, Pageable pageable) {

    long count = answersDao.selectCountByExamNo(examNo);
    List<AnswerPageDto> ret = answersDao.selectPage(examNo, pageable);
    return Pager.of(ret).pageable(pageable).totalElements(count).build();
  }

  /**
   * 引数の試験番号と問題番号で定まる出題データを取得。廃止フラグ設定済みを含む。
   *
   * @param examNo
   *          試験番号
   * @param questionNo
   *          問題番号
   * @return 出題データの Optional
   */
  @Transactional(readOnly = true)
  public Optional<Answer> getExactAnswer(Integer examNo, Integer questionNo) {
    return answersDao.selectOne(examNo, questionNo, null);
  }
}
