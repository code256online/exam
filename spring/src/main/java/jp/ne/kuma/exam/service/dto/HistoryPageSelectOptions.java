package jp.ne.kuma.exam.service.dto;

import lombok.Data;

@Data
public class HistoryPageSelectOptions {

  private Integer examineeId;
  private Integer examNo;
  private Integer examCoverage;
  private Integer examCount;
}
