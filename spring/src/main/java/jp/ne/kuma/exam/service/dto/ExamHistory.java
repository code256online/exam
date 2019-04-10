package jp.ne.kuma.exam.service.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ExamHistory implements Serializable {

  private static final long serialVersionUID = 3291416273573796088L;
  private Integer questionCount;
  private Integer correctCount;
  private LocalDateTime examDateTime;
}
