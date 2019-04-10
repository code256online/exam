package jp.ne.kuma.exam.presentation.form;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import jp.ne.kuma.exam.service.dto.QuestionData;
import lombok.Data;

/**
 * 固定出題の作成編集画面での画面入力情報を表すクラス
 *
 * @author Mike
 */
@Data
public class FixedQuestionForm implements Serializable {

  private static final long serialVersionUID = -5297604997393770695L;

  private Integer id;
  private String name;
  private List<QuestionData> questions;
  private Boolean deleted;
  private LocalDateTime modifiedAt;
}
