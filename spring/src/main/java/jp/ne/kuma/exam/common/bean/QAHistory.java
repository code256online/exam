package jp.ne.kuma.exam.common.bean;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import jp.ne.kuma.exam.common.enumerator.AnswerStates;
import jp.ne.kuma.exam.common.enumerator.QuestionMode;
import jp.ne.kuma.exam.service.dto.Question;
import lombok.Data;

@Data
@Component
@SessionScope
public class QAHistory implements Serializable {

  private static final long serialVersionUID = -6558190700978164255L;

  /** 受験者ID */
  private Integer examineeId;
  /** 受験者名 */
  private String examineeName;
  /** 試験番号 */
  private Integer examNo;
  /** 試験範囲番号 */
  private Integer examCoverage;
  /** 固定出題ID */
  private Integer fixedQuestionsId;
  /** 総出題数 */
  private Integer questionCount;
  /** 前回表示時の問題 */
  private Page<Question> lastQuestion;
  /** 回答した問題 */
  private List<QuestionPageInfo> answerStates = new ArrayList<>();
  /** 出題モード */
  private QuestionMode questionMode = QuestionMode.NORMAL;
  /** 回答開始日時 */
  private LocalDateTime startDatetime;

  public void reset() {

    examineeId = null;
    examineeName = null;
    examNo = null;
    examCoverage = null;
    fixedQuestionsId = null;
    questionCount = null;
    lastQuestion = null;
    answerStates = new ArrayList<>();
    questionMode = QuestionMode.NORMAL;
    startDatetime = null;
  }

  @Data
  public static class QuestionPageInfo implements Serializable {

    private static final long serialVersionUID = 8694798259244949156L;

    private Integer examNo;
    private Integer questionNo;
    private AnswerStates state = AnswerStates.NOT_ANSWERED;
  }
}
