package jp.ne.kuma.exam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.Random;

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
import jp.ne.kuma.exam.common.util.PropertiesUtil;
import jp.ne.kuma.exam.common.util.TestUtil;
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

@RunWith(SpringRunner.class)
@UnitTest
public class InitializeServiceTest {

  @InjectMocks
  private InitializeService service;
  @MockBean
  private AnswersDao answersDao;
  @MockBean
  private ExamsDao examsDao;
  @MockBean
  private ExamCoveragesDao examCoveragesDao;
  @MockBean
  private FixedQuestionsDao fixedQuestionsDao;
  @MockBean
  private Random random;
  @SpyBean
  private PropertiesUtil propertiesUtil;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void 全ての試験種別を取得() {

    // 想定値の設定
    List<Exam> expected = TestUtil.excel.loadAsPojoList(Exam.class, this.getClass(), "Exam", "Data1");

    // モックの設定
    doReturn(expected).when(examsDao).selectAllExamNames();

    // 試験実行
    List<Exam> actual = service.getAllExams();

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(examsDao, times(1)).selectAllExamNames();
  }

  @Test
  public void 試験番号と試験範囲に該当する問題数を取得() {

    // 想定値の設定
    final Integer examNo = 1;
    final Integer examCoverage = 2;
    final Integer expected = 20;

    // モックの設定
    doReturn(expected).when(answersDao).selectQuestionCount(anyInt(), anyInt());

    // 試験実行
    final Integer actual = service.getCount(examNo, examCoverage);

    // 値の検証
    assertThat(actual).isEqualByComparingTo(expected);

    // 呼び出しの検証
    verify(answersDao, times(1)).selectQuestionCount(examNo, examCoverage);
  }

  @Test
  public void 試験番号に紐付くすべての試験範囲を取得() {

    // 想定値の設定
    final int examNo = 1;
    List<ExamCoverage> expected = TestUtil.excel.loadAsPojoList(ExamCoverage.class, this.getClass(), "Coverage",
        "Data1");
    final boolean includeDeleted = false;

    // モックの設定
    doReturn(expected).when(examCoveragesDao).selectCoverageNamesByExamNo(anyInt(), anyBoolean());

    // 試験実行
    List<ExamCoverage> actual = service.getCoverages(examNo, includeDeleted);

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(examCoveragesDao, times(1)).selectCoverageNamesByExamNo(examNo, includeDeleted);
  }

  @Test
  public void 全ての固定試験範囲を取得() {

    // 想定値の設定
    List<FixedQuestion> result = TestUtil.excel.loadAsPojoList(FixedQuestion.class, this.getClass(),
        "Fixed", "Data1");
    List<FixedQuestionForm> expected = TestUtil.excel.loadAsPojoList(FixedQuestionForm.class, this.getClass(),
        "FixedForm", "Data1");

    // モックの設定
    doReturn(result).when(fixedQuestionsDao).selectAll();

    // 試験実行
    List<FixedQuestionForm> actual = service.getFixedQuestions();

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(fixedQuestionsDao, times(1)).selectAll();
  }

  @Test
  public void 固定出題IDに紐付く全ての問題番号を取得() {

    // 想定値の設定
    final int fixedQuestionsId = 2;
    List<QuestionData> expected = TestUtil.excel.loadAsPojoList(QuestionData.class, this.getClass(), "FixedForm", "Questions1");
    Optional<FixedQuestion> fixedQuestion = Optional
        .ofNullable(TestUtil.excel.loadAsPojo(FixedQuestion.class, this.getClass(),
            "Fixed", "Data2"));

    // モックの設定
    doReturn(fixedQuestion).when(fixedQuestionsDao).selectOne(anyInt(), any());

    // 試験実行
    List<QuestionData> actual = service.getFixedQuestionNumbers(fixedQuestionsId);

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(fixedQuestionsDao, times(1)).selectOne(fixedQuestionsId, null);
  }

  @Test
  public void 試験番号で指定の試験種別を取得() {

    // 想定値の設定
    final int examNo = 1;
    final Optional<Exam> expected = Optional
        .ofNullable(TestUtil.excel.loadAsPojo(Exam.class, this.getClass(), "Exam", "Data1"));

    // モックの設定
    doReturn(expected).when(examsDao).selectOne(anyInt(), any());

    // 試験実行
    Optional<Exam> actual = service.getExactExam(examNo);

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(examsDao, times(1)).selectOne(examNo, null);
  }

  @Test
  public void 試験番号と試験範囲IDで指定の試験範囲を取得() {

    // 想定値の設定
    final int examNo = 1;
    final int examCoverageId = 2;
    final Optional<ExamCoverage> expected = Optional
        .ofNullable(TestUtil.excel.loadAsPojo(ExamCoverage.class, this.getClass(), "Coverage", "Data2"));

    // モックの設定
    doReturn(expected).when(examCoveragesDao).selectExactExamCoverage(anyInt(), anyInt());

    // 試験実行
    Optional<ExamCoverage> actual = service.getExactExamCoverage(examNo, examCoverageId);

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(examCoveragesDao, times(1)).selectExactExamCoverage(examNo, examCoverageId);
  }

  @Test
  public void 固定出題IDで指定の固定出題範囲を取得() {

    // 想定値の設定
    final int fixedQuestionsId = 1;
    final Optional<FixedQuestion> result = Optional.ofNullable(
        TestUtil.excel.loadAsPojo(FixedQuestion.class, this.getClass(), "Fixed", "Data2"));
    final Optional<FixedQuestionForm> expected = Optional.ofNullable(TestUtil.excel.loadAsPojo(
        FixedQuestionForm.class, this.getClass(), "FixedForm", "Data2"));

    // モックの設定
    doReturn(result).when(fixedQuestionsDao).selectOne(anyInt(), any());

    // 試験実行
    Optional<FixedQuestionForm> actual = service.getExactFixedQuestion(fixedQuestionsId);

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(fixedQuestionsDao, times(1)).selectOne(fixedQuestionsId, null);
  }

  @Test
  public void 試験種別ごとの試験問題一覧ページを取得() {

    // 想定値の設定
    final int examNo = 1;
    final Pageable pageable = PageRequest.of(0, 10);
    final long count = 100;
    List<AnswerPageDto> ret = TestUtil.excel.loadAsPojoList(AnswerPageDto.class, this.getClass(), "AnswerPage",
        "Data1");
    Page<AnswerPageDto> expected = Pager.of(ret).pageable(pageable).totalElements(count).build();

    // モックの設定
    doReturn(count).when(answersDao).selectCountByExamNo(anyInt());
    doReturn(ret).when(answersDao).selectPage(anyInt(), any(Pageable.class));

    // 試験実行
    Page<AnswerPageDto> actual = service.getAnswerPage(examNo, pageable);

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(answersDao, times(1)).selectCountByExamNo(examNo);
    verify(answersDao, times(1)).selectPage(examNo, pageable);
  }

  @Test
  public void 試験番号と問題番号で問題データを取得() {

    // 想定値の設定
    final int examNo = 1;
    final int questionNo = 21;
    final Optional<Answer> expected = Optional
        .ofNullable(TestUtil.excel.loadAsPojo(Answer.class, this.getClass(), "Answer", "Data1"));

    // モックの設定
    doReturn(expected).when(answersDao).selectOne(anyInt(), anyInt(), any());

    // 試験実行
    Optional<Answer> actual = service.getExactAnswer(examNo, questionNo);

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(answersDao, times(1)).selectOne(examNo, questionNo, null);
  }
}
