package jp.ne.kuma.exam.service;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.ne.kuma.exam.common.bean.ImageProperties;
import jp.ne.kuma.exam.common.bean.Pager;
import jp.ne.kuma.exam.common.bean.QAHistory;
import jp.ne.kuma.exam.common.enumerator.QuestionMode;
import jp.ne.kuma.exam.common.util.PropertiesUtil;
import jp.ne.kuma.exam.persistence.AnswersDao;
import jp.ne.kuma.exam.persistence.dto.Answer;
import jp.ne.kuma.exam.service.dto.Choice;
import jp.ne.kuma.exam.service.dto.Image;
import jp.ne.kuma.exam.service.dto.Question;

/**
 * 出題画面のデータ取得関連サービス
 *
 * @author Mike
 */
@Service
public class QuestionService {

  /** 正答データの区切り文字 */
  private static final String CORRECT_ANSWERS_DELIMITER = ",";

  /** 画像関連設定値 */
  @Autowired
  private ImageProperties imageProperties;
  /** 試験の答えテーブル DAO */
  @Autowired
  private AnswersDao answersDao;
  /** プロパティコピーユーティリティ */
  @Autowired
  private PropertiesUtil propertiesUtil;
  /** ランダム数値生成器 */
  @Autowired
  private Random random;

  /**
   * 引数の試験番号と試験範囲で廃止フラグが設定されていない出題データが何問登録されているか返す。
   *
   * @param examNo
   *          試験番号
   * @param examCoverage
   *          試験範囲
   * @return 出題データ登録数
   */
  @Transactional(readOnly = true)
  public Integer getCount(Integer examNo, Integer examCoverage) {
    return answersDao.selectQuestionCount(examNo, examCoverage);
  }

  /**
   * 出題ページ取得
   *
   * @param qaHistory
   *          このセッションの回答履歴
   * @param pageable
   *          ページング情報
   * @param nextPage
   *          ページング情報
   * @return 出題ページの表示用データ
   */
  @Transactional(readOnly = true)
  public Page<Question> getPage(QAHistory qaHistory, Pageable pageable, Pageable nextPage) {

    int examNo = qaHistory.getAnswerStates().get(nextPage.getPageNumber()).getExamNo();
    // 固定出題モードかどうかで、実行する SQL を切り替え
    Answer result = qaHistory.getQuestionMode() == QuestionMode.FIXED
        ? answersDao.selectOneByPrimary(examNo, pageable.getPageNumber())
        : answersDao.selectPageByExamCoverage(examNo, qaHistory.getExamCoverage(), (int) pageable.getOffset() - 1);
    // 画面表示用にマッピング
    List<Question> ret = Arrays.asList(convertAnswer(result, qaHistory));

    return Pager.of(ret).pageable(nextPage).totalElements(qaHistory.getAnswerStates().size()).build();
  }

  /**
   * 互いにユニークなランダムな正の整数のセットを作成。
   *
   * @param source
   *          生成する値の上限
   * @param size
   *          生成する数
   * @return ランダム数値セット
   */
  public Set<Integer> generateUniqueRandomNumbers(int source, int size) {

    Set<Integer> set = new LinkedHashSet<>();
    while (set.size() < size) {

      int i = 1;
      // synchronized は必要ないような気がする
      synchronized (random) {
        i = random.nextInt(source) + 1;
      }

      if (!set.contains(i)) {
        set.add(i);
      }
    }

    return set;
  }

  /**
   * 試験の答えテーブルエンティティとこのセッションの解答履歴から、<br />
   * 出題画面表示用データにマッピング
   *
   * @param answer
   *          試験の答えテーブルエンティティ
   * @param qaHistory
   *          このセッションの回答履歴
   * @return 出題画面表示用データ
   */
  private Question convertAnswer(Answer answer, QAHistory qaHistory) {

    Question ret = propertiesUtil.copyProperties(Question.class, answer);
    // 選択肢リストを設定
    ret.setChoices(IntStream.range(0, answer.getChoicesCount()).boxed()
        .map(Choice::new).collect(Collectors.toList()));
    // 正答リストを設定
    ret.setCorrectAnswers(Arrays.asList(StringUtils.split(answer.getCorrectAnswers(), CORRECT_ANSWERS_DELIMITER)));
    // この問題が複数選択問題かどうかを設定
    ret.setMultiple(ret.getCorrectAnswers().size() > 1);

    // 画像 URL を合成
    String imageFile = MessageFormat.format(imageProperties.getSrcPattern(),
        String.format(imageProperties.getNumberFormat(), answer.getExamNo()),
        String.format(imageProperties.getNumberFormat(), answer.getQuestionNo()));
    Image image = new Image();
    image.setSrc(imageFile);
    ret.setImage(image);
    // 画面上の経過時間タイマーの再調整用に、試験開始時間を設定しておく
    ret.setStartDatetime(qaHistory.getStartDatetime());

    return ret;
  }
}
