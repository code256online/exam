package jp.ne.kuma.exam.persistence.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.seasar.doma.Entity;
import org.seasar.doma.jdbc.entity.NamingType;

import lombok.Data;

@Entity(naming = NamingType.SNAKE_LOWER_CASE)
@Data
public class AnswerPageDto implements Serializable {

  private static final long serialVersionUID = 2601804291993943110L;

  /** 試験番号 */
  private Integer examNo;
  /** 試験名 */
  private String examName;
  /** 問題番号 */
  private Integer questionNo;
  /** 出題範囲 */
  private Integer examCoverage;
  /** 出題範囲名 */
  private String examCoverageName;
  /** 選択肢数 */
  private Integer choicesCount;
  /** 正答 */
  private String correctAnswers;
  /** 更新日時 */
  private LocalDateTime modifiedAt;
}
