package jp.ne.kuma.exam.presentation.form;

import java.io.Serializable;

import jp.ne.kuma.exam.common.enumerator.QuestionMode;
import lombok.Data;

/**
 * 試験設定画面での画面入力情報を表すクラス
 *
 * @author Mike
 */
@Data
public class InitForm implements Serializable {

  private static final long serialVersionUID = 4845669042220024297L;

  /** 試験種別 */
  private Integer examNo;
  /** 試験範囲 */
  private Integer examCoverage;
  /** 出題数 */
  private Integer questionCount;
  /** 固定出題 ID */
  private Integer fixedQuestionsId;
  /** 出題モード */
  private QuestionMode questionMode;
}
