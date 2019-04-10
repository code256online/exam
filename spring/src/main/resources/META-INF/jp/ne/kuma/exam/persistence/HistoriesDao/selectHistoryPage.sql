SELECT
  t4.id AS examinee_id
  , concat(t4.lastname, ' ', t4.firstname) AS examinee_name
  , t2.exam_no
  , t2.exam_name
  , t2.passing_score
  , t3.id AS exam_coverage
  , t3.name AS exam_coverage_name
  , t1.exam_count
  , t1.question_count
  , t1.answer_count
  , t1.correct_count
  , (t1.answer_count / t1.question_count) * 100 AS answer_rate
  , (t1.correct_count / t1.question_count) * 100 AS correct_rate
  , t1.incorrect_questions
  , t1.start_datetime
  , t1.created_at as timestamp
  , (
    SELECT
      count(*)
    FROM
      histories
    WHERE
      deleted IS NOT TRUE
      /*%if options.examineeId != null*/
      AND examinee_id = /*options.examineeId*/1
      /*%end*/
      /*%if options.examNo != null*/
      AND exam_no = /*options.examNo*/1
      /*%end*/
      /*%if options.examCoverage != null*/
      AND exam_coverage = /*options.examCoverage*/1
      /*%end*/
      /*%if options.examCount != null*/
      AND exam_count = /*options.examCount*/1
      /*%end*/
  ) AS count
FROM
  histories t1
  INNER JOIN exams t2
    ON t1.exam_no = t2.exam_no
      AND t2.deleted IS NOT TRUE
  INNER JOIN exam_coverages t3
    ON t1.exam_coverage = t3.id
      AND (
        t1.exam_no = t3.exam_no
        OR t3.exam_no = -1
      )
      AND t3.deleted IS NOT TRUE
  INNER JOIN bitnami_redmine.users t4
    ON t1.examinee_id = t4.id
WHERE
  t1.deleted IS NOT TRUE
  /*%if options.examineeId != null*/
  AND t1.examinee_id = /*options.examineeId*/1
  /*%end*/
  /*%if options.examNo != null*/
  AND t1.exam_no = /*options.examNo*/1
  /*%end*/
  /*%if options.examCoverage != null*/
  AND t1.exam_coverage = /*options.examCoverage*/1
  /*%end*/
  /*%if options.examCount != null*/
  AND t1.exam_count = /*options.examCount*/1
  /*%end*/
ORDER BY
  t1.created_at DESC
LIMIT /*pageable.getPageSize()*/20
OFFSET /*pageable.getOffset()*/0;
