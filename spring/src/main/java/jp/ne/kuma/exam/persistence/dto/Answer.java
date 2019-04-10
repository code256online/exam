package jp.ne.kuma.exam.persistence.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.jdbc.entity.NamingType;

import lombok.Data;

@Entity(naming = NamingType.SNAKE_LOWER_CASE)
@Data
@Table(name = "answers")
public class Answer implements Serializable {

  private static final long serialVersionUID = -7846292499685843302L;

  /** 試験番号 */
  @Id
  private Integer examNo;
  /** 問題番号 */
  @Id
  private Integer questionNo;
  /** 出題範囲 */
  private Integer examCoverage;
  /** 選択肢数 */
  private Integer choicesCount;
  /** 正答 */
  private String correctAnswers;
  /** 廃止 */
  private Boolean deleted;
  /** 作成日時 */
  private LocalDateTime createdAt;
  /** 更新日時 */
  private LocalDateTime modifiedAt;
}
