package jp.ne.kuma.exam.presentation.form;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 試験範囲データの作成編集画面の画面入力情報を表すクラス
 *
 * @author Mike
 *
 */
@Data
public class ExamCoverageForm implements Serializable {

  private static final long serialVersionUID = 2016785885071042880L;

  /** 試験範囲 ID */
  private Integer id;
  /** 試験種別 */
  private Integer examNo;
  /** 試験範囲名 */
  private String name;
  /** データ取得時の更新日時（楽観排他用） */
  private LocalDateTime modifiedAt;
}
