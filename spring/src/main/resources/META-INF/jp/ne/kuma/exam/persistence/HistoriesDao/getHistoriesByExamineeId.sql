SELECT
  t2.exam_no
  , t2.exam_name
  , t2.passing_score
  , t3.id as exam_coverage
  , t3.name AS exam_coverage_name
  , t1.question_count
  , t1.answer_count
  , t1.correct_count
FROM
  histories t1
INNER JOIN
  exams t2
  ON t2.exam_no = t1.exam_no
    AND t2.deleted IS NOT TRUE
INNER JOIN
  exam_coverages t3
  ON t3.id = t1.exam_coverage
    AND t3.deleted IS NOT TRUE
WHERE
  t1.examinee_id = /*examineeId*/1
  AND t1.deleted IS NOT TRUE
ORDER BY
  t1.exam_no ASC
  , t1.exam_coverage ASC
  , t1.exam_count DESC;
