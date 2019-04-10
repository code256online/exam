package jp.ne.kuma.exam.persistence.dto;

import java.io.Serializable;

import org.seasar.doma.Entity;
import org.seasar.doma.jdbc.entity.NamingType;

import lombok.Data;

@Data
@Entity(naming = NamingType.SNAKE_LOWER_CASE)
public class HistoryDto implements Serializable {

  private static final long serialVersionUID = -4398208494478636347L;
  private Integer examNo;
  private String examName;
  private Integer examCoverage;
  private String examCoverageName;
  private Integer questionCount;
  private Integer answerCount;
  private Integer correctCount;
}
