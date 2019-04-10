package jp.ne.kuma.exam.service.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class HistoryItem implements Serializable {

  private static final long serialVersionUID = 6151190855597895417L;

  private Integer examineeId;
  private String examineeName;
  private Integer examNo;
  private String examName;
  private Double passingScore;
  private Integer examCoverage;
  private String examCoverageName;
  private Integer fixedQuestionsId;
  private String fixedQuestionsName;
  private Integer examCount;
  private Integer questionCount;
  private Integer answerCount;
  private Integer correctCount;
  private Double answerRate;
  private Double correctRate;
  private List<QuestionData> incorrectQuestions;
  private List<String> correctAnswers;
  private Long durationMinutes;
  private Long durationSeconds;
  private LocalDateTime timestamp;
}
