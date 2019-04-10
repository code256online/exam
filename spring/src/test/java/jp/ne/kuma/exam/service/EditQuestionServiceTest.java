package jp.ne.kuma.exam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.MessageSource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import jp.ne.kuma.exam.common.annotation.UnitTest;
import jp.ne.kuma.exam.common.util.PropertiesUtil;
import jp.ne.kuma.exam.common.util.TestUtil;
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

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ EditQuestionService.class, Files.class })
@UnitTest
public class EditQuestionServiceTest {

  @InjectMocks
  private EditQuestionService service;
  @MockBean
  private AnswersDao answersDao;
  @MockBean
  private ExamsDao examsDao;
  @MockBean
  private ExamCoveragesDao examCoveragesDao;
  @MockBean
  private FixedQuestionsDao fixedQuestionsDao;
  @SpyBean
  private PropertiesUtil propertiesUtil;
  @MockBean
  private Clock clock;
  @MockBean
  private MessageSource messageSource;
  @Value("${exam.question.image.uploadPath}")
  private String uploadPath;
  @Value("${exam.question.image.filenamePattern}")
  private String filenamePattern;
  @Value("${exam.question.image.numberFormat}")
  private String formatPattern;

  @Captor
  private ArgumentCaptor<Path> pathCaptor;
  @Captor
  private ArgumentCaptor<byte[]> bytesCaptor;
  @Captor
  private ArgumentCaptor<StandardOpenOption> openOptionCaptor;
  @Captor
  private ArgumentCaptor<StandardCopyOption> copyOptionCaptor;

  @Before
  public void setup() {

    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(service, "uploadPath", uploadPath);
    ReflectionTestUtils.setField(service, "filenamePattern", filenamePattern);
    ReflectionTestUtils.setField(service, "formatPattern", formatPattern);
  }

    @Test
    public void 最初の固定出題データ新規登録() {

      // 想定値設定
      final FixedQuestionForm form = TestUtil.excel.loadAsPojo(FixedQuestionForm.class, this.getClass(), "FixedForm", "Data1");
      final Optional<Integer> max = Optional.empty();
      final FixedQuestion question = TestUtil.excel.loadAsPojo(FixedQuestion.class, this.getClass(), "Fixed", "Data1");

      // モック設定
      PowerMockito.doReturn(max).when(fixedQuestionsDao).selectMaxId();
      PowerMockito.doReturn(1).when(fixedQuestionsDao).insertFixedQuestion(any(FixedQuestion.class));

      // 試験実行
      service.insertFixedQuestion(form);

      // 検証
      verify(fixedQuestionsDao, times(1)).selectMaxId();
      verify(propertiesUtil, times(1)).copyProperties(FixedQuestion.class, form);
      verify(fixedQuestionsDao, times(1)).insertFixedQuestion(question);
    }

    @Test
    public void 最初ではない固定出題データ新規登録() {

      // 想定値設定
      final FixedQuestionForm form = TestUtil.excel.loadAsPojo(FixedQuestionForm.class, this.getClass(), "FixedForm", "Data1");
      final Optional<Integer> max = Optional.ofNullable(1);
      final FixedQuestion question = TestUtil.excel.loadAsPojo(FixedQuestion.class, this.getClass(), "Fixed", "Data2");

      // モック設定
      doReturn(max).when(fixedQuestionsDao).selectMaxId();
      doReturn(1).when(fixedQuestionsDao).insertFixedQuestion(any(FixedQuestion.class));

      // 試験実行
      service.insertFixedQuestion(form);

      // 検証
      verify(fixedQuestionsDao, times(1)).selectMaxId();
      verify(propertiesUtil, times(1)).copyProperties(FixedQuestion.class, form);
      verify(fixedQuestionsDao, times(1)).insertFixedQuestion(question);
    }

    @Test
    public void 固定出題データ更新() {

      // 想定値設定
      final FixedQuestionForm form = TestUtil.excel.loadAsPojo(FixedQuestionForm.class, this.getClass(), "FixedForm", "Data2");
      final Optional<FixedQuestion> target = Optional.ofNullable(TestUtil.excel.loadAsPojo(FixedQuestion.class, this.getClass(), "Fixed", "Data3"));
      final LocalDateTime now = LocalDateTime.of(2019, 1, 28, 12, 34, 56);
      final FixedQuestion question = TestUtil.excel.loadAsPojo(FixedQuestion.class, this.getClass(), "Fixed", "Data4");

      // モック設定
      doReturn(target).when(fixedQuestionsDao).selectOne(anyInt(), any(LocalDateTime.class));
      doReturn(now.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
      doReturn(ZoneOffset.ofHours(9)).when(clock).getZone();
      doReturn(1).when(fixedQuestionsDao).updateFixedQuestion(any(FixedQuestion.class));

      // 試験実行
      service.updateFixedQuestion(form);

      // 検証
      verify(fixedQuestionsDao, times(1)).selectOne(form.getId(), form.getModifiedAt());
      verify(propertiesUtil, times(1)).copyProperties(FixedQuestion.class, form);
      verify(fixedQuestionsDao, times(1)).updateFixedQuestion(question);
    }

    @Test
    public void 固定出題データ更新時に楽観排他エラー() {

      // 想定値設定
      final FixedQuestionForm form = TestUtil.excel.loadAsPojo(FixedQuestionForm.class, this.getClass(), "FixedForm", "Data2");
      final Optional<FixedQuestion> target = Optional.empty();
      final String errorCode = "exam.error.edit.lock";
      final Object[] messageArgs = null;
      final Locale locale = Locale.getDefault();
      final String message = "エラーメッセージ";
      final OptimisticLockingFailureException expected = new OptimisticLockingFailureException(message);

      // モック設定
      doReturn(target).when(fixedQuestionsDao).selectOne(anyInt(), any(LocalDateTime.class));
      doReturn(message).when(messageSource).getMessage(any(String.class), any(), any(Locale.class));

      // 試験実行
      try {
        service.updateFixedQuestion(form);
        fail("例外がスローされなかった");
      } catch (Exception actual) {
        // 検証
        assertThat(actual).isEqualToComparingFieldByFieldRecursively(expected);
        verify(fixedQuestionsDao, times(1)).selectOne(form.getId(), form.getModifiedAt());
        verify(messageSource, times(1)).getMessage(errorCode, messageArgs, locale);
        // これ以降に処理が回らないことの確認
        verify(propertiesUtil, never()).copyProperties(FixedQuestion.class, form);
        verify(clock, never()).instant();
        verify(clock, never()).getZone();
        verify(fixedQuestionsDao, never()).updateFixedQuestion(any(FixedQuestion.class));
      }
    }

    @Test
    public void 固定出題データ削除更新() {

      // 想定値設定
      final int id = 2;
      final LocalDateTime now = LocalDateTime.of(2019, 1, 28, 12, 34, 56);
      final FixedQuestion question = TestUtil.excel.loadAsPojo(FixedQuestion.class, this.getClass(), "Fixed", "Data5");

      // モック設定
      doReturn(now.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
      doReturn(ZoneOffset.ofHours(9)).when(clock).getZone();
      doReturn(1).when(fixedQuestionsDao).updateFixedQuestion(any(FixedQuestion.class));

      // 試験実行
      service.deleteFixedQuestion(id);

      // 検証
      verify(clock, times(1)).instant();
      verify(clock, times(1)).getZone();
      verify(fixedQuestionsDao, times(1)).updateFixedQuestion(question);
    }

    @Test
    public void 最初の試験種別データ新規登録() {

      // 想定値設定
      final ExamForm form = TestUtil.excel.loadAsPojo(ExamForm.class, this.getClass(), "ExamForm", "Data1");
      final Optional<Integer> max = Optional.empty();
      final Exam exam = TestUtil.excel.loadAsPojo(Exam.class, this.getClass(), "Exam", "Data1");

      // モック設定
      doReturn(max).when(examsDao).selectMaxId();
      doReturn(1).when(examsDao).insertExam(any(Exam.class));

      // 試験実行
      service.insertExam(form);

      // 検証
      verify(examsDao, times(1)).selectMaxId();
      verify(propertiesUtil, times(1)).copyProperties(Exam.class, form);
      verify(examsDao, times(1)).insertExam(exam);
    }

    @Test
    public void 最初ではない試験種別データ新規登録() {

      // 想定値設定
      final ExamForm form = TestUtil.excel.loadAsPojo(ExamForm.class, this.getClass(), "ExamForm", "Data1");
      final Optional<Integer> max = Optional.ofNullable(1);
      final Exam exam = TestUtil.excel.loadAsPojo(Exam.class, this.getClass(), "Exam", "Data2");

      // モック設定
      doReturn(max).when(examsDao).selectMaxId();
      doReturn(1).when(examsDao).insertExam(any(Exam.class));

      // 試験実行
      service.insertExam(form);

      // 検証
      verify(examsDao, times(1)).selectMaxId();
      verify(propertiesUtil, times(1)).copyProperties(Exam.class, form);
      verify(examsDao, times(1)).insertExam(exam);
    }

    @Test
    public void 試験種別データ更新() {

      // 想定値設定
      final ExamForm form = TestUtil.excel.loadAsPojo(ExamForm.class, this.getClass(), "ExamForm", "Data2");
      final Optional<Exam> target = Optional.ofNullable(TestUtil.excel.loadAsPojo(Exam.class, this.getClass(), "Exam", "Data3"));
      final LocalDateTime now = LocalDateTime.of(2019, 1, 28, 12, 34, 56);
      final Exam exam = TestUtil.excel.loadAsPojo(Exam.class, this.getClass(), "Exam", "Data4");

      // モック設定
      doReturn(target).when(examsDao).selectOne(anyInt(), any(LocalDateTime.class));
      doReturn(now.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
      doReturn(ZoneOffset.ofHours(9)).when(clock).getZone();
      doReturn(1).when(examsDao).updateExam(any(Exam.class));

      // 試験実行
      service.updateExam(form);

      // 検証
      verify(examsDao, times(1)).selectOne(form.getExamNo(), form.getModifiedAt());
      verify(propertiesUtil, times(1)).copyProperties(Exam.class, form);
      verify(clock, times(1)).instant();
      verify(clock, times(1)).getZone();
      verify(examsDao, times(1)).updateExam(exam);
    }

    @Test
    public void 試験種別データ更新時に楽観排他エラー() {

      // 想定値設定
      final ExamForm form = TestUtil.excel.loadAsPojo(ExamForm.class, this.getClass(), "ExamForm", "Data2");
      final Optional<Exam> target = Optional.empty();
      final String errorCode = "exam.error.edit.lock";
      final Object[] messageArgs = null;
      final Locale locale = Locale.getDefault();
      final String message = "エラーメッセージ";
      final OptimisticLockingFailureException expected = new OptimisticLockingFailureException(message);

      // モック設定
      doReturn(target).when(examsDao).selectOne(anyInt(), any(LocalDateTime.class));
      doReturn(message).when(messageSource).getMessage(any(String.class), any(), any(Locale.class));

      try {
        // 試験実行
        service.updateExam(form);
        fail("例外がスローされなかった");
      } catch (Exception actual) {
        // 検証
        assertThat(actual).isEqualToComparingFieldByFieldRecursively(expected);
        verify(examsDao, times(1)).selectOne(form.getExamNo(), form.getModifiedAt());
        verify(messageSource, times(1)).getMessage(errorCode, messageArgs, locale);
        // これ以降に処理が回らない確認
        verify(propertiesUtil, never()).copyProperties(Exam.class, form);
        verify(clock, never()).instant();
        verify(clock, never()).getZone();
        verify(examsDao, never()).updateExam(any());
      }
    }

    @Test
    public void 試験種別データ削除更新() {

      // 想定値設定
      final int examNo = 1;
      final LocalDateTime now = LocalDateTime.of(2019, 1, 28, 12, 34, 56);
      final Exam exam = TestUtil.excel.loadAsPojo(Exam.class, this.getClass(), "Exam", "Data5");

      // モック設定
      doReturn(now.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
      doReturn(ZoneOffset.ofHours(9)).when(clock).getZone();
      doReturn(1).when(examsDao).updateExam(any(Exam.class));

      // 試験実行
      service.deleteExam(examNo);

      // 検証
      verify(clock, times(1)).instant();
      verify(clock, times(1)).getZone();
      verify(examsDao, times(1)).updateExam(exam);
    }

    @Test
    public void 試験種別ごとに最初の試験範囲データ新規登録() {

      // 想定値設定
      final ExamCoverageForm form = TestUtil.excel.loadAsPojo(ExamCoverageForm.class, this.getClass(), "CoverageForm", "Data1");
      final Optional<Integer> max = Optional.empty();
      final ExamCoverage coverage = TestUtil.excel.loadAsPojo(ExamCoverage.class, this.getClass(), "Coverage", "Data1");

      // モック設定
      doReturn(max).when(examCoveragesDao).selectMaxId(anyInt());
      doReturn(1).when(examCoveragesDao).insertExamCoverage(any(ExamCoverage.class));

      // 試験実行
      service.insertExamCoverage(form);

      // 検証
      verify(examCoveragesDao, times(1)).selectMaxId(form.getExamNo());
      verify(propertiesUtil, times(1)).copyProperties(ExamCoverage.class, form);
      verify(examCoveragesDao, times(1)).insertExamCoverage(coverage);
    }

    @Test
    public void 最初ではない試験範囲データ新規登録() {

      // 想定値設定
      final ExamCoverageForm form = TestUtil.excel.loadAsPojo(ExamCoverageForm.class, this.getClass(), "CoverageForm", "Data2");
      final Optional<Integer> max = Optional.ofNullable(1);
      final ExamCoverage coverage = TestUtil.excel.loadAsPojo(ExamCoverage.class, this.getClass(), "Coverage", "Data2");

      // モック設定
      doReturn(max).when(examCoveragesDao).selectMaxId(anyInt());
      doReturn(1).when(examCoveragesDao).insertExamCoverage(any(ExamCoverage.class));

      // 試験実行
      service.insertExamCoverage(form);

      // 検証
      verify(examCoveragesDao, times(1)).selectMaxId(form.getExamNo());
      verify(propertiesUtil, times(1)).copyProperties(ExamCoverage.class, form);
      verify(examCoveragesDao, times(1)).insertExamCoverage(coverage);
    }

    @Test
    public void 試験範囲データ更新() {

      // 想定値設定
      final ExamCoverageForm form = TestUtil.excel.loadAsPojo(ExamCoverageForm.class, this.getClass(), "CoverageForm", "Data3");
      final Optional<ExamCoverage> target = Optional.ofNullable(TestUtil.excel.loadAsPojo(ExamCoverage.class, this.getClass(), "Coverage", "Data3"));
      final LocalDateTime now = LocalDateTime.of(2019, 1, 28, 12, 34, 56);
      final ExamCoverage coverage = TestUtil.excel.loadAsPojo(ExamCoverage.class, this.getClass(), "Coverage", "Data4");

      // モック設定
      doReturn(target).when(examCoveragesDao).selectOne(anyInt(), anyInt(), any(LocalDateTime.class));
      doReturn(now.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
      doReturn(ZoneOffset.ofHours(9)).when(clock).getZone();
      doReturn(1).when(examCoveragesDao).updateExamCoverage(any(ExamCoverage.class));

      // 試験実行
      service.updateExamCoverage(form);

      // 検証
      verify(examCoveragesDao, times(1)).selectOne(form.getExamNo(), form.getId(), form.getModifiedAt());
      verify(propertiesUtil, times(1)).copyProperties(ExamCoverage.class, form);
      verify(clock, times(1)).instant();
      verify(clock, times(1)).getZone();
      verify(examCoveragesDao, times(1)).updateExamCoverage(coverage);
    }

    @Test
    public void 試験範囲データ更新時に楽観排他エラー() {

      // 想定値設定
      final ExamCoverageForm form = TestUtil.excel.loadAsPojo(ExamCoverageForm.class, this.getClass(), "CoverageForm", "Data3");
      final Optional<ExamCoverage> target = Optional.empty();
      final String errorCode = "exam.error.edit.lock";
      final Object[] messageArgs = null;
      final Locale locale = Locale.getDefault();
      final String message = "エラーメッセージ";
      final OptimisticLockingFailureException expected = new OptimisticLockingFailureException(message);

      // モック設定
      doReturn(target).when(examCoveragesDao).selectOne(anyInt(), anyInt(), any(LocalDateTime.class));
      doReturn(message).when(messageSource).getMessage(any(String.class), any(), any(Locale.class));

      try {
        // 試験実行
        service.updateExamCoverage(form);
        fail("例外がスローされなかった。");
      } catch (Exception actual) {
        // 検証
        assertThat(actual).isEqualToComparingFieldByFieldRecursively(expected);
        verify(examCoveragesDao, times(1)).selectOne(form.getExamNo(), form.getId(), form.getModifiedAt());
        verify(messageSource, times(1)).getMessage(errorCode, messageArgs, locale);
        // これ移行に処理が移行しない確認
        verify(clock, never()).instant();
        verify(clock, never()).getZone();
        verify(examCoveragesDao, never()).updateExamCoverage(any());
      }
    }

    @Test
    public void 試験範囲データ削除更新() {

      // 想定値設定
      final int examNo = 1;
      final int id = 2;
      final LocalDateTime now = LocalDateTime.of(2019, 1, 28, 12, 34, 56);
      final ExamCoverage coverage = TestUtil.excel.loadAsPojo(ExamCoverage.class, this.getClass(), "Coverage", "Data5");

      // モック設定
      doReturn(now.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
      doReturn(ZoneOffset.ofHours(9)).when(clock).getZone();
      doReturn(1).when(examCoveragesDao).updateExamCoverage(any(ExamCoverage.class));

      // 試験実行
      service.deleteExamCoverage(examNo, id);

      // 検証
      verify(clock, times(1)).instant();
      verify(clock, times(1)).getZone();
      verify(examCoveragesDao, times(1)).updateExamCoverage(coverage);
    }

    @Test
    public void 試験種別ごとの登録済み問題番号の最大値を取得() {

      // 想定値設定
      final int examNo = 1;
      final int expected = 55;

      // モック設定
      doReturn(expected).when(answersDao).selectMaxQuestionNo(anyInt());

      // 試験実行
      int actual = service.getMaxQuestionNo(examNo);

      // 検証
      assertThat(actual).isEqualByComparingTo(expected);
      verify(answersDao, times(1)).selectMaxQuestionNo(examNo);
    }

    @Test
    public void 試験データ新規登録() throws IOException {

      // 想定値設定
      final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data1");
      final LocalDateTime now = LocalDateTime.of(2019, 1, 28, 12, 34, 56);
      final Answer answer = TestUtil.excel.loadAsPojo(Answer.class, this.getClass(), "Answer", "Data1");
      final Path dir = Paths.get("C:/test_images/questions");
      final Path file = dir.resolve("E001Q002.jpg");

      // モック設定
      doReturn(now.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
      doReturn(ZoneOffset.ofHours(9)).when(clock).getZone();
      doReturn(1).when(answersDao).insertAnswer(any(Answer.class));
      PowerMockito.mockStatic(Files.class);
      PowerMockito.when(Files.createDirectories(pathCaptor.capture())).thenReturn(dir);
      PowerMockito.when(Files.exists(pathCaptor.capture())).thenReturn(false);
      PowerMockito.when(Files.write(pathCaptor.capture(), bytesCaptor.capture(), openOptionCaptor.capture()))
          .thenReturn(file);

      // 試験実行
      service.insertAnswer(form);

      // 検証
      verify(propertiesUtil, times(1)).copyProperties(Answer.class, form);
      verify(clock, times(1)).instant();
      verify(clock, times(1)).getZone();
      verify(answersDao, times(1)).insertAnswer(answer);

      assertThat(pathCaptor.getAllValues().get(0)).isEqualTo(dir);
      assertThat(pathCaptor.getAllValues().get(1)).isEqualTo(file);
      assertThat(pathCaptor.getAllValues().get(2)).isEqualTo(file);
      assertThat(bytesCaptor.getValue()).isEqualTo(form.getFile().getBytes());
      assertThat(openOptionCaptor.getAllValues().get(0)).isEqualTo(StandardOpenOption.CREATE);
      assertThat(openOptionCaptor.getAllValues().get(1)).isEqualTo(StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Test
    public void ファイルアップロード時に既にファイルが存在する場合は1世代バックアップ() throws IOException {

      // 想定値設定
      final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data1");
      final Path dir = Paths.get("C:/test_images/questions");
      final Path file = dir.resolve("E001Q002.jpg");
      final Path backupDir = dir.resolve("backup");
      final Path backupFile = backupDir.resolve("E001Q002.jpg");

      // モック設定
      PowerMockito.mockStatic(Files.class);
      PowerMockito.when(Files.createDirectories(pathCaptor.capture())).thenReturn(dir);
      PowerMockito.when(Files.exists(pathCaptor.capture())).thenReturn(true);
      PowerMockito.when(Files.copy(pathCaptor.capture(), pathCaptor.capture(), copyOptionCaptor.capture()))
          .thenReturn(backupFile);
      PowerMockito.when(Files.write(pathCaptor.capture(), bytesCaptor.capture(), openOptionCaptor.capture()))
          .thenReturn(file);

      // 試験実行
      ReflectionTestUtils.invokeMethod(service, "uploadImage", form);

      // 検証
      assertThat(pathCaptor.getAllValues().get(0)).isEqualTo(dir);
      assertThat(pathCaptor.getAllValues().get(1)).isEqualTo(file);
      assertThat(pathCaptor.getAllValues().get(2)).isEqualTo(backupDir);
      assertThat(pathCaptor.getAllValues().get(3)).isEqualTo(file);
      assertThat(pathCaptor.getAllValues().get(4)).isEqualTo(backupFile);
      assertThat(copyOptionCaptor.getValue()).isEqualTo(StandardCopyOption.REPLACE_EXISTING);
      assertThat(pathCaptor.getAllValues().get(5)).isEqualTo(file);
      assertThat(bytesCaptor.getValue()).isEqualTo(form.getFile().getBytes());
      assertThat(openOptionCaptor.getAllValues().get(0)).isEqualTo(StandardOpenOption.CREATE);
      assertThat(openOptionCaptor.getAllValues().get(1)).isEqualTo(StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Test
    public void ファイルアップロード時にファイル書き込みエラー() throws IOException {

      // 想定値設定
      final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data1");
      final Path dir = Paths.get("C:/test_images/questions");
      final IOException exception = new IOException("エラー");
      final IllegalArgumentException expected = new IllegalArgumentException(exception);

      // モック設定
      PowerMockito.mockStatic(Files.class);
      PowerMockito.when(Files.createDirectories(pathCaptor.capture())).thenThrow(exception);

      try {
        // 試験実行
        ReflectionTestUtils.invokeMethod(service, "uploadImage", form);
        fail("例外がスローされなかった");
      } catch (Exception actual) {
        // 検証
        assertThat(actual.getCause()).isEqualToComparingFieldByFieldRecursively(expected);
        assertThat(pathCaptor.getValue()).isEqualTo(dir);
      }
    }

    @Test
    public void 試験データ更新() {

      // 想定値設定
      final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data2");
      final Optional<Answer> target = Optional.ofNullable(TestUtil.excel.loadAsPojo(Answer.class, this.getClass(), "Answer", "Data2"));
      final LocalDateTime now = LocalDateTime.of(2019, 1, 28, 12, 34, 56);
      final Answer answer = TestUtil.excel.loadAsPojo(Answer.class, this.getClass(), "Answer", "Data3");

      // モック設定
      doReturn(target).when(answersDao).selectOne(anyInt(), anyInt(), any(LocalDateTime.class));
      doReturn(now.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
      doReturn(ZoneOffset.ofHours(9)).when(clock).getZone();
      doReturn(1).when(answersDao).updateAnswer(any(Answer.class));

      // 試験実行
      service.updateAnswer(form);

      // 検証
      verify(answersDao, times(1)).selectOne(form.getExamNo(), form.getQuestionNo(), form.getModifiedAt());
      verify(propertiesUtil, times(1)).copyProperties(Answer.class, form);
      verify(clock, times(1)).instant();
      verify(clock, times(1)).getZone();
      verify(answersDao, times(1)).updateAnswer(answer);
    }

    @Test
    public void 試験データ更新時に楽観排他エラー() {

      // 想定値設定
      final AnswerForm form = TestUtil.excel.loadAsPojo(AnswerForm.class, this.getClass(), "AnswerForm", "Data2");
      final Optional<Answer> target = Optional.empty();
      final String errorCode = "exam.error.edit.lock";
      final Object[] messageArgs = null;
      final Locale locale = Locale.getDefault();
      final String message = "エラーメッセージ";
      final OptimisticLockingFailureException expected = new OptimisticLockingFailureException(message);

      // モック設定
      doReturn(target).when(answersDao).selectOne(anyInt(), anyInt(), any(LocalDateTime.class));
      doReturn(message).when(messageSource).getMessage(any(String.class), any(), any(Locale.class));

      try {
        // 試験実行
        service.updateAnswer(form);
        fail("例外がスローされなかった");
      } catch (Exception actual) {
        // 検証
        assertThat(actual).isEqualToComparingFieldByFieldRecursively(expected);
        verify(answersDao, times(1)).selectOne(form.getExamNo(), form.getQuestionNo(), form.getModifiedAt());
        verify(messageSource, times(1)).getMessage(errorCode, messageArgs, locale);
      }
    }

    @Test
    public void 試験データ削除更新() {

      // 想定値設定
      final int examNo = 1;
      final int questionNo = 2;
      final LocalDateTime now = LocalDateTime.of(2019, 1, 28, 12, 34, 56);
      final Answer answer = TestUtil.excel.loadAsPojo(Answer.class, this.getClass(), "Answer", "Data4");

      // モック設定
      doReturn(now.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
      doReturn(ZoneOffset.ofHours(9)).when(clock).getZone();
      doReturn(1).when(answersDao).updateAnswer(any(Answer.class));

      // 試験実行
      service.deleteAnswer(examNo, questionNo);

      // 検証
      verify(clock, times(1)).instant();
      verify(clock, times(1)).getZone();
      verify(answersDao, times(1)).updateAnswer(answer);
    }
}
