package jp.ne.kuma.exam.common.enumerator;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

/**
 * 出題モード
 *
 * @author Mike
 *
 */
public enum QuestionMode {

  NORMAL, FIXED,
  ;

  /**
   * 文字列から enum に変換。
   *
   * @param text
   *          enum を表す文字列
   * @return enum（デフォルト NORMAL）
   */
  public static QuestionMode asText(String text) {

    return Stream.of(QuestionMode.values())
        .filter(x -> StringUtils.equalsIgnoreCase(text, x.toString()))
        .findAny().orElse(QuestionMode.NORMAL);
  }
}
