package jp.ne.kuma.exam.persistence.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.seasar.doma.Entity;
import org.seasar.doma.Table;
import org.seasar.doma.jdbc.entity.NamingType;

import lombok.Data;

@Data
@Entity(naming = NamingType.SNAKE_LOWER_CASE)
@Table(name = "fixed_histories")
public class FixedHistory implements Serializable {

  private static final long serialVersionUID = 7264531350605324503L;

  private Integer examineeId;
  private Integer fixedQuestionsId;
  private Integer examCount;
  private Integer questionCount;
  private Integer answerCount;
  private Integer correctCount;
  private String incorrectQuestions;
  private LocalDateTime startDatetime;
  private Boolean deleted;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
}
