package jp.ne.kuma.exam.common.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.omg.CORBA.SystemException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ページング制御オブジェクト。<br />
 *
 * @see org.springframework.data.domain.PageImpl
 *
 * @author Mike
 *
 * @param <T>
 *          ページングするDTO
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pager<T> extends PageImpl<T> {

  private static final long serialVersionUID = -1993032009891915201L;

  /** 1ページに表示する最大のナビゲーションの数 */
  private static final Integer MAX_PAGE_ITEMS = 10;
  /** 単一のページナビゲーションオブジェクトのリスト */
  private List<PageItem> items = new ArrayList<>();
  /** 現在のページ */
  private Integer currentNumber;
  /** 表示件数変更リンク用 */
  private List<Integer> sizeList;

  /**
   * コンストラクタ
   *
   * @param content
   *          ページに表示する行のDTOリスト
   * @param page
   *          表示するページ
   * @param size
   *          1ページに表示する明細の数
   * @param total
   *          ページングしない場合の全要素数
   * @param sort
   *          ソート条件
   * @param sizeList
   *          表示件数変更用リスト
   * @param displayMode
   *          表示モード
   * @deprecated これはセッションからの復帰専用です。<br />
   *             インスタンスの生成にはビルダーを使用してください。{@link #of(List)}
   */
  @JsonCreator(mode = Mode.PROPERTIES)
  public Pager(@JsonProperty("content") List<T> content, @JsonProperty("number") int page,
      @JsonProperty("size") int size, @JsonProperty("totalElements") long total, @JsonProperty("sort") Sort sort,
      @JsonProperty("sizeList") List<Integer> sizeList) {

    super(content, PageRequest.of(page, size, sort), total);
    this.sizeList = sizeList;
    init();
  }

  /**
   * コンストラクタ。
   *
   * @param content
   *          ページに表示する行のDTOのリスト
   * @param paging
   *          ページング情報
   * @param totalElements
   *          ページングしない場合の全要素数
   */
  private Pager(List<T> content, Pageable paging, long totalElements) {

    super(content, paging, totalElements);
    init();
  }

  /**
   * 表示するページナビゲーションを初期化します。
   *
   */
  private void init() {

    if (sizeList == null || sizeList.size() == 0) {
      // デフォルトの表示件数変更は 5 10 20
      sizeList = Arrays.asList(5, 10, 20);
    }

    // 現在のページは、1オリジンで保持。
    currentNumber = getNumber() + 1;

    // ページナビゲーションの開始ページと終了ページを計算
    int start;
    int size;
    if (getTotalPages() <= MAX_PAGE_ITEMS) {
      start = 1;
      size = getTotalPages();
    } else {
      if (currentNumber <= MAX_PAGE_ITEMS - MAX_PAGE_ITEMS / 2) {
        start = 1;
        size = MAX_PAGE_ITEMS;
      } else if (currentNumber >= getTotalPages() - MAX_PAGE_ITEMS / 2) {
        start = getTotalPages() - MAX_PAGE_ITEMS + 1;
        size = MAX_PAGE_ITEMS;
      } else {
        start = currentNumber - MAX_PAGE_ITEMS / 2;
        size = MAX_PAGE_ITEMS;
      }
    }

    // 計算の結果表示対象となったページナビゲーションを保持
    for (int i = 0; i < size; i++) {
      items.add(new PageItem(start + i, (start + i) == currentNumber));
    }
  }

  /**
   * ページング制御オブジェクト生成ビルダーを返します。
   *
   * @param content
   *          明細 DTO リスト
   * @return ページング制御オブジェクトビルダー
   */
  public static <T> PagerBuilder<T> of(List<T> content) {
    return new PagerBuilder<>(content);
  }

  /**
   * ページング制御オブジェクト生成ビルダー
   *
   * @author Mike
   */
  public static class PagerBuilder<T> {

    private static final String ERROR_MESSAGE_ARG = "ページング制御オブジェクトを生成するための情報が不足しています。";

    private List<T> content;
    private Pageable pageable;
    private Long totalElements;
    private List<Integer> sizeList;

    private PagerBuilder(List<T> content) {

      if (content == null) {
        content = new ArrayList<>();
      }
      this.content = content;
    }

    /**
     * ページング情報を設定します。
     *
     * @param pageable
     *          org.springframework.data.domain.Pageable
     * @return ページング制御オブジェクト生成ビルダー
     */
    public PagerBuilder<T> pageable(Pageable pageable) {
      this.pageable = pageable;
      return this;
    }

    /**
     * ページング情報を設定します。
     *
     * @param page
     *          ページ番号（0オリジン）
     * @param size
     *          1ページの行数
     * @return ページング制御オブジェクト生成ビルダー
     */
    public PagerBuilder<T> pageable(int page, int size) {
      this.pageable = PageRequest.of(page, size);
      return this;
    }

    /**
     * ページング情報を設定します。
     *
     * @param page
     *          ページ番号（0オリジン）
     * @param size
     *          1ページの行数
     * @param sort
     *          ソート条件
     * @return ページング制御オブジェクト生成ビルダー
     */
    public PagerBuilder<T> pageable(int page, int size, Sort sort) {
      this.pageable = PageRequest.of(page, size, sort);
      return this;
    }

    /**
     * ページング情報を設定します。
     *
     * @param page
     *          ページ番号（0オリジン）
     * @param size
     *          1ページの行数
     * @param ソート方向
     * @param ソートするプロパティ名
     * @return ページング制御オブジェクト生成ビルダー
     */
    public PagerBuilder<T> pageable(int page, int size, Direction direction, String... properties) {
      this.pageable = PageRequest.of(page, size, direction, properties);
      return this;
    }

    /**
     * 全件取得した場合の全行数を設定します。
     *
     * @param totalElements
     *          全件取得した場合の行数
     * @return ページング制御オブジェクト生成ビルダー
     */
    public PagerBuilder<T> totalElements(long totalElements) {
      this.totalElements = totalElements;
      return this;
    }

    /**
     * ページサイズ変更リンク用の設定可能なページサイズリストを設定します。
     *
     * @param sizeList
     *          ページサイズリスト
     * @return ページング制御オブジェクト生成ビルダー
     */
    public PagerBuilder<T> sizeList(List<Integer> sizeList) {
      this.sizeList = sizeList;
      return this;
    }

    /**
     * ページサイズ変更リンク用の設定可能なページサイズリストを設定します。
     *
     * @param sizes
     *          ページサイズ（可変長）
     * @return ページング制御オブジェクト生成ビルダー
     */
    public PagerBuilder<T> sizeList(int... sizes) {
      this.sizeList = IntStream.of(sizes).boxed().collect(Collectors.toList());
      return this;
    }

    /**
     * これまでの設定値をもとに、ページング制御オブジェクトを生成します。<br />
     * ページング情報、全件取得した場合の行数は設定必須です。
     *
     * @return ページング制御オブジェクト
     * @throws SystemException
     *           ページング情報、または全行数が未設定の場合
     */
    public Pager<T> build() throws SystemException {

      if (pageable == null || totalElements == null) {
        throw new IllegalStateException(ERROR_MESSAGE_ARG);
      }

      Pager<T> pager = new Pager<>(content, pageable, totalElements);
      pager.sizeList = sizeList == null || sizeList.isEmpty() ? pager.sizeList : sizeList;
      return pager;
    }
  }

  /**
   * ページングのナビゲーションリンクを表すクラス
   *
   * @author Mike
   *
   */
  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  public static class PageItem implements Serializable {

    private static final long serialVersionUID = -9202465542175417833L;
    /** ページ番号 */
    private Integer number;
    /** 現在ページかどうか */
    private boolean current;
  }
}
