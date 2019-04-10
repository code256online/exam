package jp.ne.kuma.exam.service.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class FinishPage implements Serializable {

  private static final long serialVersionUID = 1040553815366551962L;

  private HistoryItem primary;
  private Map<Integer, List<HistoryItem>> history;
}
