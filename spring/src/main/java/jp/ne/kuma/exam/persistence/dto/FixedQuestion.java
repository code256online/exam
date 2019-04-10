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
@Table(name = "fixed_questions")
public class FixedQuestion implements Serializable {

  private static final long serialVersionUID = -6549505522398895264L;

  @Id
  private Integer id;
  private String name;
  private String questions;
  private Boolean deleted;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
}
