package jp.ne.kuma.exam.service.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * アルファベット 1 文字で表される選択肢を表すクラス
 *
 * @author Mike
 *
 */
@Data
@NoArgsConstructor
public class Choice implements Serializable {

  private static final long serialVersionUID = -7313465452915276241L;
  private static final char U0041 = 'A';

  private Character label;

  public Choice(Integer value) {

    this.label = (char) (U0041 + value);
  }

  public boolean isCorrect(String answer) {
    return answer.charAt(0) == label;
  }
}
