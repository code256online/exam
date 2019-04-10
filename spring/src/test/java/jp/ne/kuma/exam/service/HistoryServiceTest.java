package jp.ne.kuma.exam.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import jp.ne.kuma.exam.common.annotation.UnitTest;
import jp.ne.kuma.exam.common.bean.Pager;
import jp.ne.kuma.exam.common.bean.QAHistory;
import jp.ne.kuma.exam.common.enumerator.QuestionMode;
import jp.ne.kuma.exam.common.util.PropertiesUtil;
import jp.ne.kuma.exam.common.util.TestUtil;
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

@RunWith(SpringRunner.class)
@UnitTest
public class HistoryServiceTest {

  @InjectMocks
  private HistoryService service;
  @SpyBean
  PropertiesUtil propertiesUtil;
  @MockBean
  private HistoriesDao historiesDao;
  @MockBean
  private FixedHistoriesDao fixedHistoriesDao;
  @MockBean
  private AnswersDao answersDao;
  @MockBean
  private ExamsDao examsDao;
  @MockBean
  private Clock clock;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void 履歴更新() {

    // 想定値の設定
    final LocalDateTime dateTime = LocalDateTime.of(2018, 1, 28, 12, 34, 56, 789_000_000);
    final Optional<History> maxCount = Optional.empty();
    QAHistory qaHistory = TestUtil.excel.loadAsPojo(QAHistory.class, this.getClass(), "QAHistory", "Data1");
    final History history = TestUtil.excel.loadAsPojo(History.class, this.getClass(), "History", "Data1");

    final int examineeId = qaHistory.getExamineeId();
    final int examNo = qaHistory.getExamNo();
    final int examCoverage = qaHistory.getExamCoverage();

    // モックの設定
    doReturn(dateTime.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
    doReturn(ZoneId.systemDefault()).when(clock).getZone();
    doReturn(maxCount).when(historiesDao).selectOneByPrimaryAndMax(anyInt(), anyInt(), anyInt());
    doReturn(1).when(historiesDao).insertHistory(any(History.class));

    // 試験実行
    service.update(qaHistory);

    // 呼び出しの検証
    verify(historiesDao, times(1)).selectOneByPrimaryAndMax(examineeId, examNo, examCoverage);
    verify(historiesDao, times(1)).insertHistory(history);
  }

  @Test
  public void 全問正解の場合_不正解の問題番号をnull更新() {

    // 想定値の設定
    final LocalDateTime dateTime = LocalDateTime.of(2018, 1, 28, 12, 34, 56, 789_000_000);
    final Optional<History> maxCount = Optional
        .ofNullable(TestUtil.excel.loadAsPojo(History.class, this.getClass(), "History", "Data2"));
    QAHistory qaHistory = TestUtil.excel.loadAsPojo(QAHistory.class, this.getClass(), "QAHistory", "Data2");
    final History history = TestUtil.excel.loadAsPojo(History.class, this.getClass(), "History", "Data3");
    final int examineeId = qaHistory.getExamineeId();
    final int examNo = qaHistory.getExamNo();
    final int examCoverage = qaHistory.getExamCoverage();

    // モックの設定
    doReturn(dateTime.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
    doReturn(ZoneId.systemDefault()).when(clock).getZone();
    doReturn(maxCount).when(historiesDao).selectOneByPrimaryAndMax(anyInt(), anyInt(), anyInt());
    doReturn(1).when(historiesDao).insertHistory(any(History.class));

    // 試験実行
    service.update(qaHistory);

    // 呼び出しの検証
    verify(historiesDao, times(1)).selectOneByPrimaryAndMax(examineeId, examNo, examCoverage);
    verify(historiesDao, times(1)).insertHistory(history);
  }

  @Test
  public void 固定出題モードの履歴更新() {

    final LocalDateTime dateTime = LocalDateTime.of(2018, 1, 28, 12, 34, 56, 789_000_000);
    final Optional<FixedHistory> maxCount = Optional.empty();
    QAHistory qaHistory = TestUtil.excel.loadAsPojo(QAHistory.class, this.getClass(), "QAHistory", "Data3");
    final FixedHistory history = TestUtil.excel.loadAsPojo(FixedHistory.class, this.getClass(),
        "FixedHistory", "Data1");

    final int examineeId = qaHistory.getExamineeId();
    final int fixedQuestionsId = qaHistory.getFixedQuestionsId();

    // モックの設定
    doReturn(dateTime.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
    doReturn(ZoneId.systemDefault()).when(clock).getZone();
    doReturn(maxCount).when(fixedHistoriesDao).selectOneByPrimaryAndMax(anyInt(), anyInt());
    doReturn(1).when(fixedHistoriesDao).insertHistory(any(FixedHistory.class));

    // 試験実行
    service.update(qaHistory);

    // 呼び出しの検証
    verify(fixedHistoriesDao, times(1)).selectOneByPrimaryAndMax(examineeId, fixedQuestionsId);
    verify(fixedHistoriesDao, times(1)).insertHistory(history);
  }

  @Test
  public void 固定出題モードの履歴更新_全問正解の場合() {

    // 想定値の設定
    final LocalDateTime dateTime = LocalDateTime.of(2018, 1, 28, 12, 34, 56, 789_000_000);
    final Optional<FixedHistory> maxCount = Optional
        .ofNullable(TestUtil.excel.loadAsPojo(FixedHistory.class, this.getClass(), "FixedHistory", "Data2"));
    QAHistory qaHistory = TestUtil.excel.loadAsPojo(QAHistory.class, this.getClass(), "QAHistory", "Data4");
    final FixedHistory history = TestUtil.excel.loadAsPojo(FixedHistory.class, this.getClass(),
        "FixedHistory", "Data3");
    final int examineeId = qaHistory.getExamineeId();
    final int fixedQuestionsId = qaHistory.getFixedQuestionsId();

    // モックの設定
    doReturn(dateTime.toInstant(ZoneOffset.ofHours(9))).when(clock).instant();
    doReturn(ZoneId.systemDefault()).when(clock).getZone();
    doReturn(maxCount).when(fixedHistoriesDao).selectOneByPrimaryAndMax(anyInt(), anyInt());
    doReturn(1).when(fixedHistoriesDao).insertHistory(any(FixedHistory.class));

    // 試験実行
    service.update(qaHistory);

    // 呼び出しの検証
    verify(fixedHistoriesDao, times(1)).selectOneByPrimaryAndMax(examineeId, fixedQuestionsId);
    verify(fixedHistoriesDao, times(1)).insertHistory(history);
  }

  @Test
  public void 履歴のページング取得() {

    final Integer examineeId = 1;
    final Pageable pageable = PageRequest.of(0, 10);
    final QuestionMode questionMode = QuestionMode.NORMAL;
    final HistoryPageSelectOptions options = TestUtil.excel.loadAsPojo(HistoryPageSelectOptions.class, this.getClass(),
        "HistoryPageSelectOptions", "Data1");
    List<HistoryPageDto> result = TestUtil.excel.loadAsPojoList(HistoryPageDto.class, this.getClass(),
        "HistoryPageDto", "Data1");
    List<HistoryItem> content = TestUtil.excel.loadAsPojoList(HistoryItem.class, this.getClass(),
        "HistoryItem", "Data1");
    final long totalElements = result.get(0).getCount();
    Page<HistoryItem> expected = Pager.of(content).pageable(pageable).totalElements(totalElements).build();

    // モックの設定
    doReturn(result).when(historiesDao).selectHistoryPage(any(HistoryPageSelectOptions.class),
        any(Pageable.class));

    // 試験実行
    Page<HistoryItem> actual = service.getPage(examineeId, pageable, questionMode);

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(historiesDao, times(1)).selectHistoryPage(options, pageable);
  }

  @Test
  public void 固定出題の履歴一覧ページング取得() {

    final Integer examineeId = 1;
    final Pageable pageable = PageRequest.of(0, 10);
    final QuestionMode questionMode = QuestionMode.FIXED;
    final FixedHistoryPageSelectOptions options = TestUtil.excel.loadAsPojo(FixedHistoryPageSelectOptions.class,
        this.getClass(),
        "FixedHistoryPageSelectOptions", "Data1");
    List<FixedHistoryPageDto> result = TestUtil.excel.loadAsPojoList(FixedHistoryPageDto.class, this.getClass(),
        "FixedHistoryPageDto", "Data1");
    List<Exam> exams = TestUtil.excel.loadAsPojoList(Exam.class, this.getClass(), "Exam", "Data1");
    List<HistoryItem> content = TestUtil.excel.loadAsPojoList(HistoryItem.class, this.getClass(),
        "HistoryItem", "Data2");
    final long totalElements = result.get(0).getCount();
    Page<HistoryItem> expected = Pager.of(content).pageable(pageable).totalElements(totalElements).build();

    // モックの設定
    doReturn(result).when(fixedHistoriesDao).selectHistoryPage(any(FixedHistoryPageSelectOptions.class),
        any(Pageable.class));
    doReturn(exams).when(examsDao).selectAllExamNames();

    // 試験実行
    Page<HistoryItem> actual = service.getPage(examineeId, pageable, questionMode);

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(fixedHistoriesDao, times(1)).selectHistoryPage(options, pageable);
  }

  @Test
  public void 履歴詳細画面のデータ取得() {

    final int examineeId = 1;
    final int examNo = 2;
    final int examCoverage = 3;
    final int examCount = 4;
    final Pageable pageable = PageRequest.of(0, 1);
    final HistoryPageSelectOptions options = TestUtil.excel.loadAsPojo(HistoryPageSelectOptions.class, this.getClass(),
        "HistoryPageSelectOptions", "Data2");
    List<HistoryPageDto> result = TestUtil.excel.loadAsPojoList(HistoryPageDto.class, this.getClass(),
        "HistoryPageDto", "Data2");
    List<QuestionData> incorrects = TestUtil.excel.loadAsPojoList(QuestionData.class, this.getClass(),
        "QuestionData", "Data1");
    List<String> correctAnswers = Arrays.asList("A,B", "A");
    List<HistoryItem> contents = TestUtil.excel.loadAsPojoList(HistoryItem.class, this.getClass(),
        "HistoryItem", "Data3");
    Page<HistoryItem> expected = Pager.of(contents).pageable(pageable).totalElements(2).build();

    // モック設定
    doReturn(result).when(historiesDao).selectHistoryPage(any(HistoryPageSelectOptions.class), any(Pageable.class));
    doReturn(correctAnswers).when(answersDao).selectCorrectAnswers(anyList());

    // 試験実行
    Page<HistoryItem> actual = service.getDetailPage(examineeId, examNo, examCoverage, examCount, pageable);

    // 検証
    assertThat(actual).isEqualTo(expected);
    verify(historiesDao, times(1)).selectHistoryPage(options, pageable);
    verify(answersDao, times(1)).selectCorrectAnswers(incorrects);
  }

  @Test
  public void 履歴詳細画面のデータ取得_全問正解の場合は正答の取得を行わない() {

    final int examineeId = 1;
    final int examNo = 2;
    final int examCoverage = 3;
    final int examCount = 4;
    final Pageable pageable = PageRequest.of(0, 1);
    final HistoryPageSelectOptions options = TestUtil.excel.loadAsPojo(HistoryPageSelectOptions.class, this.getClass(),
        "HistoryPageSelectOptions", "Data2");
    List<HistoryPageDto> result = TestUtil.excel.loadAsPojoList(HistoryPageDto.class, this.getClass(),
        "HistoryPageDto", "Data3");
    List<HistoryItem> contents = TestUtil.excel.loadAsPojoList(HistoryItem.class, this.getClass(),
        "HistoryItem", "Data4");
    Page<HistoryItem> expected = Pager.of(contents).pageable(pageable).totalElements(2).build();

    // モック設定
    doReturn(result).when(historiesDao).selectHistoryPage(any(HistoryPageSelectOptions.class), any(Pageable.class));

    // 試験実行
    Page<HistoryItem> actual = service.getDetailPage(examineeId, examNo, examCoverage, examCount, pageable);

    // 検証
    assertThat(actual).isEqualTo(expected);
    verify(historiesDao, times(1)).selectHistoryPage(options, pageable);
  }

  @Test
  public void 固定履歴詳細画面のデータ取得() {

    final int examineeId = 3;
    final int fixedQuestionsId = 2;
    final int examCount = 4;
    final Pageable pageable = PageRequest.of(0, 1);
    final FixedHistoryPageSelectOptions options = TestUtil.excel.loadAsPojo(FixedHistoryPageSelectOptions.class,
        this.getClass(),
        "FixedHistoryPageSelectOptions", "Data2");
    List<FixedHistoryPageDto> result = TestUtil.excel.loadAsPojoList(FixedHistoryPageDto.class, this.getClass(),
        "FixedHistoryPageDto", "Data2");
    List<QuestionData> incorrects = TestUtil.excel.loadAsPojoList(QuestionData.class, this.getClass(),
        "QuestionData", "Data2");
    List<String> correctAnswers = Arrays.asList("A,B", "A");
    List<HistoryItem> contents = TestUtil.excel.loadAsPojoList(HistoryItem.class, this.getClass(),
        "HistoryItem", "Data5");
    List<Exam> exams = TestUtil.excel.loadAsPojoList(Exam.class, this.getClass(), "Exam", "Data1");
    Page<HistoryItem> expected = Pager.of(contents).pageable(pageable).totalElements(2).build();

    // モック設定
    doReturn(result).when(fixedHistoriesDao).selectHistoryPage(any(FixedHistoryPageSelectOptions.class),
        any(Pageable.class));
    doReturn(exams).when(examsDao).selectAllExamNames();
    doReturn(correctAnswers).when(answersDao).selectCorrectAnswers(anyList());

    // 試験実行
    Page<HistoryItem> actual = service.getFixedDetailPage(examineeId, fixedQuestionsId, examCount, pageable);

    // 検証
    assertThat(actual).isEqualTo(expected);
    verify(fixedHistoriesDao, times(1)).selectHistoryPage(options, pageable);
    verify(examsDao, times(1)).selectAllExamNames();
    verify(answersDao, times(1)).selectCorrectAnswers(incorrects);
  }

  @Test
  public void 固定履歴詳細画面用のデータ取得_全問正解の場合正答の取得を行わない() {

    final int examineeId = 3;
    final int fixedQuestionsId = 2;
    final int examCount = 4;
    final Pageable pageable = PageRequest.of(0, 1);
    final FixedHistoryPageSelectOptions options = TestUtil.excel.loadAsPojo(FixedHistoryPageSelectOptions.class,
        this.getClass(),
        "FixedHistoryPageSelectOptions", "Data2");
    List<FixedHistoryPageDto> result = TestUtil.excel.loadAsPojoList(FixedHistoryPageDto.class, this.getClass(),
        "FixedHistoryPageDto", "Data3");
    List<HistoryItem> contents = TestUtil.excel.loadAsPojoList(HistoryItem.class, this.getClass(),
        "HistoryItem", "Data6");
    List<Exam> exams = TestUtil.excel.loadAsPojoList(Exam.class, this.getClass(), "Exam", "Data1");
    Page<HistoryItem> expected = Pager.of(contents).pageable(pageable).totalElements(2).build();

    // モック設定
    doReturn(result).when(fixedHistoriesDao).selectHistoryPage(any(FixedHistoryPageSelectOptions.class),
        any(Pageable.class));
    doReturn(exams).when(examsDao).selectAllExamNames();

    // 試験実行
    Page<HistoryItem> actual = service.getFixedDetailPage(examineeId, fixedQuestionsId, examCount, pageable);

    // 検証
    assertThat(actual).isEqualTo(expected);
    verify(fixedHistoriesDao, times(1)).selectHistoryPage(options, pageable);
    verify(examsDao, times(1)).selectAllExamNames();
  }

  @Test
  public void 回答終了画面の履歴取得() {

    final Integer examineeId = 1;
    final Pageable pageable = PageRequest.of(0, 10);
    final QuestionMode questionMode = QuestionMode.NORMAL;
    final HistoryPageSelectOptions options = TestUtil.excel.loadAsPojo(HistoryPageSelectOptions.class, this.getClass(),
        "HistoryPageSelectOptions", "Data1");
    List<HistoryPageDto> result = TestUtil.excel.loadAsPojoList(HistoryPageDto.class, this.getClass(),
        "HistoryPageDto", "Data1");
    List<HistoryItem> content = TestUtil.excel.loadAsPojoList(HistoryItem.class, this.getClass(),
        "HistoryItem", "Data7");
    final long totalElements = result.get(0).getCount();
    List<QuestionData> incorrects = TestUtil.excel.loadAsPojoList(QuestionData.class, this.getClass(),
        "QuestionData", "Data3");
    List<String> correctAnswers = Arrays.asList("A,B", "A");
    Page<HistoryItem> expected = Pager.of(content).pageable(pageable).totalElements(totalElements).build();

    // モックの設定
    doReturn(result).when(historiesDao).selectHistoryPage(any(HistoryPageSelectOptions.class), any(Pageable.class));
    doReturn(correctAnswers).when(answersDao).selectCorrectAnswers(anyList());

    // 試験実行
    Page<HistoryItem> actual = service.getFinishPage(examineeId, pageable, questionMode);

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(historiesDao, times(1)).selectHistoryPage(options, pageable);
    verify(answersDao, times(1)).selectCorrectAnswers(incorrects);
  }

  @Test
  public void 回答終了画面の履歴取得_全問正解の場合は正答の取得をしない() {

    final Integer examineeId = 1;
    final Pageable pageable = PageRequest.of(0, 10);
    final QuestionMode questionMode = QuestionMode.NORMAL;
    final HistoryPageSelectOptions options = TestUtil.excel.loadAsPojo(HistoryPageSelectOptions.class, this.getClass(),
        "HistoryPageSelectOptions", "Data1");
    List<HistoryPageDto> result = TestUtil.excel.loadAsPojoList(HistoryPageDto.class, this.getClass(),
        "HistoryPageDto", "Data3");
    List<HistoryItem> content = TestUtil.excel.loadAsPojoList(HistoryItem.class, this.getClass(),
        "HistoryItem", "Data8");
    final long totalElements = result.get(0).getCount();
    Page<HistoryItem> expected = Pager.of(content).pageable(pageable).totalElements(totalElements).build();

    // モックの設定
    doReturn(result).when(historiesDao).selectHistoryPage(any(HistoryPageSelectOptions.class), any(Pageable.class));

    // 試験実行
    Page<HistoryItem> actual = service.getFinishPage(examineeId, pageable, questionMode);

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(historiesDao, times(1)).selectHistoryPage(options, pageable);
  }

  @Test
  public void 履歴を1以上持つ受験者のリストを返却() {

    final QuestionMode questionMode = QuestionMode.NORMAL;
    List<Examinee> expected = TestUtil.excel.loadAsPojoList(Examinee.class, this.getClass(), "Examinee", "Data1");

    // モックの設定
    doReturn(expected).when(historiesDao).selectAllExaminees();

    // 試験実行
    List<Examinee> actual = service.getExaminees(questionMode);

    // 検証
    assertThat(actual).isEqualTo(expected);
    verify(historiesDao, times(1)).selectAllExaminees();
  }

  @Test
  public void 固定履歴を1以上持つ受験者のリストを返却() {

    final QuestionMode questionMode = QuestionMode.FIXED;
    List<Examinee> expected = TestUtil.excel.loadAsPojoList(Examinee.class, this.getClass(), "Examinee", "Data1");

    // モックの設定
    doReturn(expected).when(fixedHistoriesDao).selectAllExaminees();

    // 試験実行
    List<Examinee> actual = service.getExaminees(questionMode);

    // 検証
    assertThat(actual).isEqualTo(expected);
    verify(fixedHistoriesDao, times(1)).selectAllExaminees();
  }
}
