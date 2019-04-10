package jp.ne.kuma.exam.persistence.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.seasar.doma.Entity;
import org.seasar.doma.jdbc.entity.NamingType;

import lombok.Data;

@Data
@Entity(naming = NamingType.SNAKE_LOWER_CASE)
public class FixedHistoryPageDto implements Serializable {

  private static final long serialVersionUID = 3786863297750135130L;

  private Integer examineeId;
  private String examineeName;
  private Integer fixedQuestionsId;
  private String fixedQuestionsName;
  private Integer examCount;
  private Integer questionCount;
  private Integer answerCount;
  private Integer correctCount;
  private Double answerRate;
  private Double correctRate;
  private String incorrectQuestions;
  private LocalDateTime startDatetime;
  private LocalDateTime timestamp;
  private Long count;
}
