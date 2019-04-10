SELECT
  t2.id as fixed_questions_id
  , t2.name AS fixed_questions_name
  , t1.question_count
  , t1.answer_count
  , t1.correct_count
FROM
  fixed_histories t1
INNER JOIN
  fixed_questions t2
  ON t2.id = t1.fixed_questions_id
    AND t2.deleted IS NOT TRUE
WHERE
  t1.examinee_id = /*examineeId*/1
  AND t1.deleted IS NOT TRUE
ORDER BY
  t1.exam_no ASC
  , t1.fixed_questions_id ASC
  , t1.exam_count DESC;
