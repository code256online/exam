package jp.ne.kuma.exam.presentation.form;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 試験種別の作成編集画面の画面入力情報を表すクラス
 *
 * @author Mike
 *
 */
@Data
public class ExamForm implements Serializable {

  private static final long serialVersionUID = -6366851695868467049L;

  /** 試験種別 */
  private Integer examNo;
  /** 試験種別名 */
  private String examName;
  /** 合否ライン */
  private Double passingScore;
  /** データ取得時の更新日時（楽観排他用） */
  private LocalDateTime modifiedAt;
}
