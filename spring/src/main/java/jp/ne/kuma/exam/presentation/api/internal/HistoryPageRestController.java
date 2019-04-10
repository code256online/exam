package jp.ne.kuma.exam.presentation.api.internal;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.ne.kuma.exam.common.bean.UserInfo;
import jp.ne.kuma.exam.common.enumerator.QuestionMode;
import jp.ne.kuma.exam.persistence.dto.Examinee;
import jp.ne.kuma.exam.service.HistoryService;
import jp.ne.kuma.exam.service.dto.HistoryItem;

/**
 * 履歴関連 REST コントローラ
 *
 * @author Mike
 */
@RestController
@RequestMapping("/api/internal/history")
public class HistoryPageRestController {

  /** 履歴関連サービス */
  @Autowired
  private HistoryService service;
  /** ログインユーザー情報 */
  @Autowired
  private UserInfo userInfo;

  @GetMapping(value = "examinees", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public List<Examinee> examinees(@RequestParam QuestionMode mode) {
    return service.getExaminees(mode);
  }

  /**
   * 一覧用ページ取得
   *
   * @param page
   *          ページ
   * @param examineeId
   *          受験者 ID
   * @param questionMode
   *          出題モード
   * @return 一覧ページ情報
   */
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Page<HistoryItem> getPage(@RequestParam Integer page, @RequestParam(required = false) Integer examineeId,
      @RequestParam(required = false) QuestionMode questionMode) {

    Pageable pageable = PageRequest.of(page, 10, Direction.DESC, "t1.created_at");

    if (!userInfo.isAdmin()) {
      examineeId = userInfo.getId();
    }
    return service.getPage(examineeId, pageable, questionMode);
  }

  /**
   * 詳細用ページ取得
   *
   * @param examineeId
   *          受験者 ID
   * @param examNo
   *          試験番号
   * @param examCoverage
   *          試験範囲
   * @param examCount
   *          受験回数
   * @return 詳細ページ情報
   */
  @GetMapping(value = "/detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Page<HistoryItem> getDetailPage(@RequestParam Integer examineeId, @RequestParam Integer examNo,
      @RequestParam Integer examCoverage, @RequestParam Integer examCount) {

    return service.getDetailPage(examineeId, examNo, examCoverage, examCount, PageRequest.of(0, 1));
  }

  /**
   * 固定出題モードの詳細ページ取得
   *
   * @param examineeId
   *          受験者 ID
   * @param examNo
   *          試験番号
   * @param fixedQuestionsId
   *          固定出題 ID
   * @param examCount
   *          受験回数
   * @return 詳細ページ情報
   */
  @GetMapping(value = "/fixedDetail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Page<HistoryItem> getFixedDetailPage(@RequestParam Integer examineeId, @RequestParam Integer fixedQuestionsId,
      @RequestParam Integer examCount) {

    return service.getFixedDetailPage(examineeId, fixedQuestionsId, examCount, PageRequest.of(0, 1));
  }
}
