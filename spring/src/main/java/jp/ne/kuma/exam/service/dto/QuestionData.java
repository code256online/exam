package jp.ne.kuma.exam.service.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class QuestionData implements Serializable {

  private static final long serialVersionUID = 3164210382336524120L;

  private Integer examNo;
  private String examName;
  private Integer questionNo;
}
