package jp.ne.kuma.exam.service.dto;

import lombok.Data;

@Data
public class FixedHistoryPageSelectOptions {

  private Integer examineeId;
  private Integer fixedQuestionsId;
  private Integer examCount;
}
