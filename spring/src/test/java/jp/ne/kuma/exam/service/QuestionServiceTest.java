package jp.ne.kuma.exam.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import jp.ne.kuma.exam.common.annotation.UnitTest;
import jp.ne.kuma.exam.common.bean.ImageProperties;
import jp.ne.kuma.exam.common.bean.Pager;
import jp.ne.kuma.exam.common.bean.QAHistory;
import jp.ne.kuma.exam.common.util.PropertiesUtil;
import jp.ne.kuma.exam.common.util.TestUtil;
import jp.ne.kuma.exam.persistence.AnswersDao;
import jp.ne.kuma.exam.persistence.dto.Answer;
import jp.ne.kuma.exam.service.dto.Choice;
import jp.ne.kuma.exam.service.dto.Question;

@RunWith(SpringRunner.class)
@UnitTest
public class QuestionServiceTest {

  @InjectMocks
  private QuestionService service;
  @MockBean
  private ImageProperties imageProperties;
  @MockBean
  private AnswersDao answersDao;
  @SpyBean
  private PropertiesUtil propertiesUtil;
  @MockBean
  private Random random;

  @Mock
  private QAHistory qaHistory;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void 試験番号と試験範囲IDに該当する問題数を取得() {

    // 想定値の設定
    final int examNo = 1;
    final int examCoverageId = 2;
    final int expected = 22;

    // モックの設定
    doReturn(expected).when(answersDao).selectQuestionCount(anyInt(), anyInt());

    // 試験実行
    int actual = service.getCount(examNo, examCoverageId);

    // 値の検証
    assertThat(actual).isEqualByComparingTo(expected);

    // 呼び出しの検証
    verify(answersDao, times(1)).selectQuestionCount(examNo, examCoverageId);
  }

  @Test
  public void 通常モードでの出題データ取得_複数選択() {

    final int page = 5;
    final int size = 1;
    final QAHistory qaHistory = TestUtil.excel.loadAsPojo(QAHistory.class, this.getClass(), "QAHistory", "Data1");
    final Pageable pageable = PageRequest.of(qaHistory.getAnswerStates().get(page).getQuestionNo(), size);
    final Pageable nextPage = PageRequest.of(page, size);
    final Answer answer = TestUtil.excel.loadAsPojo(Answer.class, this.getClass(), "Answer", "Data1");
    final String srcPattern = "https://kennet.server-on.net/exam_images/questions/E{0}Q{1}.jpg";
    final String numberFormat = "%03d";
    Question question = TestUtil.excel.loadAsPojo(Question.class, this.getClass(), "Question", "Data1");
    question
        .setChoices(IntStream.range(0, answer.getChoicesCount()).boxed().map(Choice::new).collect(Collectors.toList()));
    question.setMultiple(true);
    final Page<Question> expected = Pager.of(Arrays.asList(question)).pageable(nextPage)
        .totalElements(qaHistory.getAnswerStates().size()).build();

    // モック設定
    doReturn(answer).when(answersDao).selectPageByExamCoverage(anyInt(), anyInt(), anyInt());
    doReturn(srcPattern).when(imageProperties).getSrcPattern();
    doReturn(numberFormat).when(imageProperties).getNumberFormat();

    // 試験実行
    Page<Question> actual = service.getPage(qaHistory, pageable, nextPage);

    // 検証
    assertThat(actual).isEqualTo(expected);
    verify(answersDao, times(1)).selectPageByExamCoverage(qaHistory.getExamNo(), qaHistory.getExamCoverage(),
        (int) pageable.getOffset() - 1);
  }

  @Test
  public void 固定出題モードでの出題データ取得_複数選択ではない() {

    final int page = 5;
    final int size = 1;
    final QAHistory qaHistory = TestUtil.excel.loadAsPojo(QAHistory.class, this.getClass(), "QAHistory", "Data2");
    final Pageable pageable = PageRequest.of(qaHistory.getAnswerStates().get(page).getQuestionNo(), size);
    final Pageable nextPage = PageRequest.of(page, size);
    final Answer answer = TestUtil.excel.loadAsPojo(Answer.class, this.getClass(), "Answer", "Data2");
    final String srcPattern = "https://kennet.server-on.net/exam_images/questions/E{0}Q{1}.jpg";
    final String numberFormat = "%03d";
    Question question = TestUtil.excel.loadAsPojo(Question.class, this.getClass(), "Question", "Data2");
    question
        .setChoices(IntStream.range(0, answer.getChoicesCount()).boxed().map(Choice::new).collect(Collectors.toList()));
    question.setMultiple(false);
    final Page<Question> expected = Pager.of(Arrays.asList(question)).pageable(nextPage)
        .totalElements(qaHistory.getAnswerStates().size()).build();

    // モック設定
    doReturn(answer).when(answersDao).selectOneByPrimary(anyInt(), anyInt());
    doReturn(srcPattern).when(imageProperties).getSrcPattern();
    doReturn(numberFormat).when(imageProperties).getNumberFormat();

    // 試験実行
    Page<Question> actual = service.getPage(qaHistory, pageable, nextPage);

    // 検証
    assertThat(actual).isEqualTo(expected);
    assertThat(actual.getContent()).isEqualTo(expected.getContent());
    verify(answersDao, times(1)).selectOneByPrimary(qaHistory.getAnswerStates().get(page).getExamNo(),
        (int) pageable.getPageNumber());
  }

  @Test
  public void ユニークなランダム数値リスト生成() {

    // 想定値の設定
    final int source = 5;
    final int size = 4;
    Set<Integer> expected = new LinkedHashSet<>(Arrays.asList(2, 3, 4, 6));

    // モックの設定
    doReturn(1).doReturn(2).doReturn(3).doReturn(3).doReturn(5).when(random).nextInt(anyInt());

    // 試験実行
    Set<Integer> actual = service.generateUniqueRandomNumbers(source, size);

    // 値の検証
    assertThat(actual).isEqualTo(expected);

    // 呼び出しの検証
    verify(random, times(5)).nextInt(source);
  }
}
