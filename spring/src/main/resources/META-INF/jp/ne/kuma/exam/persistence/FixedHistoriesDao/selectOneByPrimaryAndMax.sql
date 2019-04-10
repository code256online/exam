SELECT
  t1.*
FROM
  fixed_histories t1
  INNER JOIN (
    SELECT
      max(exam_count) AS exam_count
    FROM
      fixed_histories
    WHERE
      examinee_id = /*examineeId*/1
      AND fixed_questions_id = /*fixedQuestionsId*/0
  ) t2
    ON t1.exam_count = t2.exam_count
WHERE
  t1.examinee_id = /*examineeId*/1
  AND t1.fixed_questions_id = /*fixedQuestionsId*/0
  AND deleted IS NOT TRUE;
