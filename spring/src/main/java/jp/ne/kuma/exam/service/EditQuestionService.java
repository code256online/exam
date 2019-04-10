package jp.ne.kuma.exam.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.ne.kuma.exam.common.util.PropertiesUtil;
import jp.ne.kuma.exam.persistence.AnswersDao;
import jp.ne.kuma.exam.persistence.ExamCoveragesDao;
import jp.ne.kuma.exam.persistence.ExamsDao;
import jp.ne.kuma.exam.persistence.FixedQuestionsDao;
import jp.ne.kuma.exam.persistence.dto.Answer;
import jp.ne.kuma.exam.persistence.dto.Exam;
import jp.ne.kuma.exam.persistence.dto.ExamCoverage;
import jp.ne.kuma.exam.persistence.dto.FixedQuestion;
import jp.ne.kuma.exam.presentation.form.AnswerForm;
import jp.ne.kuma.exam.presentation.form.ExamCoverageForm;
import jp.ne.kuma.exam.presentation.form.ExamForm;
import jp.ne.kuma.exam.presentation.form.FixedQuestionForm;

/**
 * 出題データメンテナンス関連サービス
 *
 * @author Mike
 */
@Service
public class EditQuestionService {

  @Autowired
  private AnswersDao answersDao;
  @Autowired
  private ExamsDao examsDao;
  @Autowired
  private ExamCoveragesDao examCoveragesDao;
  @Autowired
  private FixedQuestionsDao fixedQuestionsDao;
  @Autowired
  private PropertiesUtil propertiesUtil;
  @Autowired
  private Clock clock;
  @Autowired
  private MessageSource messageSource;
  @Value("${exam.question.image.uploadPath}")
  private String uploadPath;
  @Value("${exam.question.image.filenamePattern}")
  private String filenamePattern;
  @Value("${exam.question.image.numberFormat}")
  private String formatPattern;

  /**
   * 固定出題データの新規登録
   *
   * @param form
   *          登録データ
   */
  @Transactional(rollbackFor = Exception.class)
  public int insertFixedQuestion(FixedQuestionForm form) {

    Optional<Integer> max = fixedQuestionsDao.selectMaxId();
    int id = max.isPresent() ? max.get() + 1 : 1;

    FixedQuestion question = propertiesUtil.copyProperties(FixedQuestion.class, form);
    question.setId(id);
    question.setQuestions(form.getQuestions().stream()
        .map(x -> String.join("-", String.valueOf(x.getExamNo()), String.valueOf(x.getQuestionNo())))
        .collect(Collectors.joining(",")));
    fixedQuestionsDao.insertFixedQuestion(question);

    return id;
  }

  /**
   * 固定出題データの更新登録
   *
   * @param form
   *          登録データ
   */
  @Transactional(rollbackFor = Exception.class)
  public void updateFixedQuestion(FixedQuestionForm form) {

    // 楽観排他制御
    Optional<FixedQuestion> target = fixedQuestionsDao.selectOne(form.getId(), form.getModifiedAt());
    if (!target.isPresent()) {
      throw new OptimisticLockingFailureException(
          messageSource.getMessage("exam.error.edit.lock", null, Locale.getDefault()));
    }

    FixedQuestion question = propertiesUtil.copyProperties(FixedQuestion.class, form);
    question.setQuestions(form.getQuestions().stream()
        .map(x -> String.join("-", String.valueOf(x.getExamNo()), String.valueOf(x.getQuestionNo())))
        .collect(Collectors.joining(",")));
    question.setModifiedAt(LocalDateTime.now(clock));
    fixedQuestionsDao.updateFixedQuestion(question);
  }

  /**
   * 固定出題データの削除
   *
   * @param examNo
   *          試験番号
   * @param id
   *          固定出題 ID
   */
  @Transactional(rollbackFor = Exception.class)
  public void deleteFixedQuestion(Integer id) {

    FixedQuestion question = new FixedQuestion();
    question.setId(id);
    question.setDeleted(true);
    question.setModifiedAt(LocalDateTime.now(clock));
    fixedQuestionsDao.updateFixedQuestion(question);
  }

  /**
   * 試験種別データの新規登録
   *
   * @param form
   *          登録データ
   */
  @Transactional(rollbackFor = Exception.class)
  public void insertExam(ExamForm form) {

    Optional<Integer> max = examsDao.selectMaxId();
    int id = max.isPresent() ? max.get() + 1 : 1;

    Exam exam = propertiesUtil.copyProperties(Exam.class, form);
    exam.setExamNo(id);
    examsDao.insertExam(exam);
  }

  /**
   * 試験種別データの更新登録
   *
   * @param form
   *          登録データ
   */
  @Transactional(rollbackFor = Exception.class)
  public void updateExam(ExamForm form) {

    // 楽観排他制御
    Optional<Exam> target = examsDao.selectOne(form.getExamNo(), form.getModifiedAt());
    if (!target.isPresent()) {
      throw new OptimisticLockingFailureException(
          messageSource.getMessage("exam.error.edit.lock", null, Locale.getDefault()));
    }

    Exam exam = propertiesUtil.copyProperties(Exam.class, form);
    exam.setModifiedAt(LocalDateTime.now(clock));
    examsDao.updateExam(exam);
  }

  /**
   * 試験種別データの削除
   *
   * @param examNo
   *          試験番号
   */
  @Transactional(rollbackFor = Exception.class)
  public void deleteExam(Integer examNo) {

    Exam exam = new Exam();
    exam.setExamNo(examNo);
    exam.setDeleted(true);
    exam.setModifiedAt(LocalDateTime.now(clock));
    examsDao.updateExam(exam);
  }

  /**
   * 試験範囲データの新規登録
   *
   * @param form
   *          登録データ
   */
  @Transactional(rollbackFor = Exception.class)
  public void insertExamCoverage(ExamCoverageForm form) {

    Optional<Integer> max = examCoveragesDao.selectMaxId(form.getExamNo());
    int id = max.isPresent() ? max.get() + 1 : 1;

    ExamCoverage coverage = propertiesUtil.copyProperties(ExamCoverage.class, form);
    coverage.setId(id);
    examCoveragesDao.insertExamCoverage(coverage);
  }

  /**
   * 試験範囲データの更新登録
   *
   * @param form
   *          登録データ
   */
  @Transactional(rollbackFor = Exception.class)
  public void updateExamCoverage(ExamCoverageForm form) {

    // 楽観排他制御
    Optional<ExamCoverage> target = examCoveragesDao.selectOne(
        form.getExamNo(), form.getId(), form.getModifiedAt());
    if (!target.isPresent()) {
      throw new OptimisticLockingFailureException(
          messageSource.getMessage("exam.error.edit.lock", null, Locale.getDefault()));
    }

    ExamCoverage question = propertiesUtil.copyProperties(ExamCoverage.class, form);
    question.setModifiedAt(LocalDateTime.now(clock));
    examCoveragesDao.updateExamCoverage(question);
  }

  /**
   * 試験範囲データの削除
   *
   * @param examNo
   *          試験番号
   * @param id
   *          試験範囲 ID
   */
  @Transactional(rollbackFor = Exception.class)
  public void deleteExamCoverage(Integer examNo, Integer id) {

    ExamCoverage coverage = new ExamCoverage();
    coverage.setExamNo(examNo);
    coverage.setId(id);
    coverage.setDeleted(true);
    coverage.setModifiedAt(LocalDateTime.now(clock));
    examCoveragesDao.updateExamCoverage(coverage);
  }

  /**
   * 試験番号に紐付く出題データのうち最大の問題番号を取得
   *
   * @param examNo
   *          試験番号
   * @return 最大の問題番号
   */
  @Transactional(readOnly = true)
  public int getMaxQuestionNo(int examNo) {
    return answersDao.selectMaxQuestionNo(examNo);
  }

  /**
   * 出題データの新規登録
   *
   * @param form
   *          登録データ
   */
  @Transactional(rollbackFor = Exception.class)
  public void insertAnswer(AnswerForm form) {

    Answer answer = propertiesUtil.copyProperties(Answer.class, form);
    answer.setModifiedAt(LocalDateTime.now(clock));
    answersDao.insertAnswer(answer);
    uploadImage(form);
  }

  /**
   * 出題データの更新登録
   *
   * @param form
   *          登録データ
   */
  @Transactional(rollbackFor = Exception.class)
  public void updateAnswer(AnswerForm form) {

    Optional<Answer> target = answersDao.selectOne(form.getExamNo(), form.getQuestionNo(), form.getModifiedAt());
    if (!target.isPresent()) {
      throw new OptimisticLockingFailureException(
          messageSource.getMessage("exam.error.edit.lock", null, Locale.getDefault()));
    }

    Answer answer = propertiesUtil.copyProperties(Answer.class, form);
    answer.setModifiedAt(LocalDateTime.now(clock));
    answersDao.updateAnswer(answer);
    uploadImage(form);
  }

  /**
   * 出題データの削除
   *
   * @param examNo
   *          試験番号
   * @param questionNo
   *          問題番号
   */
  @Transactional(rollbackFor = Exception.class)
  public void deleteAnswer(Integer examNo, Integer questionNo) {

    Answer answer = new Answer();
    answer.setExamNo(examNo);
    answer.setQuestionNo(questionNo);
    answer.setDeleted(true);
    answer.setModifiedAt(LocalDateTime.now(clock));
    answersDao.updateAnswer(answer);
  }

  /**
   * 出題画像データのアップロード
   *
   * @param form
   *          出題登録データ
   */
  private void uploadImage(AnswerForm form) {

    if (form.getFile() != null && ArrayUtils.isNotEmpty(form.getFile().getBytes())) {

      final String filename = MessageFormat.format(filenamePattern, String.format(formatPattern, form.getExamNo()),
          String.format(formatPattern, form.getQuestionNo()));
      Path parent = Paths.get(uploadPath);
      try {

        Files.createDirectories(parent);
        Path filePath = parent.resolve(filename);

        if (Files.exists(filePath)) {
          // 既にアップロードファイルが存在する場合、1世代だけバックアップをとる。
          Path moveTo = parent.resolve("backup").resolve(filename);
          Files.createDirectories(moveTo.getParent());
          Files.copy(filePath, moveTo, StandardCopyOption.REPLACE_EXISTING);
        }

        Files.write(parent.resolve(filename), form.getFile().getBytes(), StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }
}
