package jp.ne.kuma.exam.persistence.dto;

import java.io.Serializable;

import org.seasar.doma.Entity;
import org.seasar.doma.jdbc.entity.NamingType;

import lombok.Data;

@Entity(naming = NamingType.SNAKE_LOWER_CASE)
@Data
public class Examinee implements Serializable {

  private static final long serialVersionUID = -1229011550277131036L;

  private Integer examineeId;
  private String examineeName;
}
