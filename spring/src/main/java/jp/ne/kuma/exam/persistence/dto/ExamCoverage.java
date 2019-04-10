package jp.ne.kuma.exam.persistence.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.seasar.doma.Entity;
import org.seasar.doma.Table;
import org.seasar.doma.jdbc.entity.NamingType;

import lombok.Data;

@Data
@Entity(naming = NamingType.SNAKE_LOWER_CASE)
@Table(name = "exam_coverages")
public class ExamCoverage implements Serializable {

  private static final long serialVersionUID = 5535499548059302049L;

  private Integer id;
  private Integer examNo;
  private String name;
  private boolean deleted;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
}
