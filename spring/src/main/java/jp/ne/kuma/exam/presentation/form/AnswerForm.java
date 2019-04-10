package jp.ne.kuma.exam.presentation.form;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.apache.xmlbeans.impl.util.Base64;

import lombok.Data;

/**
 * 出題データの作成編集画面の画面入力情報を表すクラス
 *
 * @author Mike
 *
 */
@Data
public class AnswerForm implements Serializable {

  private static final long serialVersionUID = -1606899776252643301L;

  /** 試験種別 */
  private Integer examNo;
  /** 問題番号 */
  private Integer questionNo;
  /** 試験範囲 */
  private Integer examCoverage;
  /** 選択肢の数 */
  private Integer choicesCount;
  /** 正答 */
  private String correctAnswers;
  /** データ取得時の更新日時（楽観排他用） */
  private LocalDateTime modifiedAt;
  /** 問題画像データ */
  private FileInfo file;
  /** 新規作成モードなら true */
  private boolean insertMode;

  /**
   * アップロードする問題画像を表すクラス
   *
   * @author Mike
   *
   */
  @Data
  public static class FileInfo implements Serializable {

    private static final long serialVersionUID = 8563678276532304513L;

    /** ファイル名 */
    private String name;
    /** バイナリを Base64 エンコードしたもの */
    private String bytesByBase64;
    /** バイナリ */
    private byte[] bytes;

    /**
     * バイナリを設定。
     *
     * @param bytesByBase64
     *          バイナリを Base64 エンコードしたもの
     */
    public void setBytesByBase64(String bytesByBase64) {
      this.bytesByBase64 = bytesByBase64;
      this.bytes = Base64.decode(bytesByBase64.getBytes());
    }
  }
}
