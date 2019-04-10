package jp.ne.kuma.exam.persistence.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.jdbc.entity.NamingType;

import lombok.Data;

@Data
@Entity(naming = NamingType.SNAKE_LOWER_CASE)
@Table(name = "exams")
public class Exam implements Serializable {

  private static final long serialVersionUID = 6586678791480976720L;

  @Id
  private Integer examNo;
  private String examName;
  private Double passingScore;
  private boolean deleted;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
}
