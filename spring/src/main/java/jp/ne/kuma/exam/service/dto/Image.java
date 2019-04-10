package jp.ne.kuma.exam.service.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * html 上の画像を表すクラス
 *
 * @author Mike
 */
@Data
public class Image implements Serializable {

  private static final long serialVersionUID = 1043393174078943282L;
  /** URL */
  private String src;
  /** 代替テキスト */
  private String alt;
  /** タイトル */
  private String title;
}
