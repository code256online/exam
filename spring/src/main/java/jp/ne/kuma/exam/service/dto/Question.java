package jp.ne.kuma.exam.service.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import jp.ne.kuma.exam.common.enumerator.AnswerStates;
import lombok.Data;

/**
 * 問題文とその解答を表すクラス
 *
 * @author Mike
 */
@Data
public class Question implements Serializable {

  private static final long serialVersionUID = 5864259233433188889L;

  /** 試験番号 */
  private Integer examNo;
  /** 問題番号 */
  private Integer questionNo;
  /** 問題文画像 */
  private Image image;
  /** 選択肢 */
  private List<Choice> choices;
  /** 正答 */
  private List<String> correctAnswers;
  /** 複数選択制御 */
  private boolean isMultiple;
  /** 試験開始時刻 */
  private LocalDateTime startDatetime;

  /**
   * 正解かどうかを返します。
   *
   * @param choices
   *          選択した答え
   * @return 正解なら true
   */
  public AnswerStates isCorrect(String... choices) {

    boolean correct = correctAnswers.size() == Stream.of(choices).filter(x -> correctAnswers.contains(x))
        .filter(x -> this.choices.stream().anyMatch(y -> y.isCorrect(x)))
        .count();

    return correct ? AnswerStates.CORRECT : AnswerStates.INCORRECT;
  }
}
