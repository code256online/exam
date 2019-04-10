SELECT
  t1.*
FROM
  histories t1
  INNER JOIN (
    SELECT
      max(exam_count) AS exam_count
    FROM
      histories
    WHERE
      examinee_id = /*examineeId*/1
      AND exam_no = /*examNo*/1
      AND exam_coverage = /*examCoverage*/0
  ) t2
    ON t1.exam_count = t2.exam_count
WHERE
  t1.examinee_id = /*examineeId*/1
  AND t1.exam_no = /*examNo*/1
  AND t1.exam_coverage = /*examCoverage*/0
  AND deleted IS NOT TRUE;
