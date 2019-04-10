package jp.ne.kuma.exam.persistence.dto;

import java.io.Serializable;

import org.seasar.doma.Entity;
import org.seasar.doma.jdbc.entity.NamingType;

import lombok.Data;

@Data
@Entity(naming = NamingType.SNAKE_LOWER_CASE)
public class FixedHistoryDto implements Serializable {

  private static final long serialVersionUID = 2828514956518831413L;
  private Integer fixedQuestionsId;
  private String fixedQuestionsName;
  private Integer examNo;
  private String examName;
  private Integer questionCount;
  private Integer answerCount;
  private Integer correctCount;
}
