SELECT
  t2.exam_no AS exam_no
  , t2.exam_name AS exam_name
  , t1.question_no AS question_no
  , t3.id AS exam_coverage
  , t3.name AS exam_coverage_name
  , t1.choices_count AS choices_count
  , t1.correct_answers AS correct_answers
  , t1.modified_at AS modified_at
FROM
  answers t1
INNER JOIN
  exams t2
  ON t2.exam_no = t1.exam_no
  AND t2.deleted IS NOT TRUE
INNER JOIN
  exam_coverages t3
  ON t3.id = t1.exam_coverage
  AND t3.exam_no = t1.exam_no
WHERE
  t1.exam_no = /*examNo*/1
  AND t1.deleted IS NOT TRUE
ORDER BY
  t1.question_no ASC
LIMIT
  /*pageable.getPageSize()*/1
OFFSET
  /*pageable.getOffset()*/0;
