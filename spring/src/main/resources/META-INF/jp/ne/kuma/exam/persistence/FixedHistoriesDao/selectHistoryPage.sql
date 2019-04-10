SELECT
  t3.id AS examinee_id
  , concat(t3.lastname, ' ', t3.firstname) AS examinee_name
  , t2.id AS fixed_questions_id
  , t2.name AS fixed_questions_name
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
      fixed_histories
    WHERE
      deleted IS NOT TRUE
      /*%if options.examineeId != null*/
      AND examinee_id = /*options.examineeId*/1
      /*%end*/
      /*%if options.fixedQuestionsId != null*/
      AND fixed_questions_id = /*options.fixedQuestionsId*/1
      /*%end*/
      /*%if options.examCount != null*/
      AND exam_count = /*options.examCount*/1
      /*%end*/
  ) AS count
FROM
  fixed_histories t1
  INNER JOIN fixed_questions t2
    ON t1.fixed_questions_id = t2.id
      AND t2.deleted IS NOT TRUE
  INNER JOIN bitnami_redmine.users t3
    ON t1.examinee_id = t3.id
WHERE
  t1.deleted IS NOT TRUE
  /*%if options.examineeId != null*/
  AND t1.examinee_id = /*options.examineeId*/1
  /*%end*/
  /*%if options.fixedQuestionsId != null*/
  AND t1.fixed_questions_id = /*options.fixedQuestionsId*/1
  /*%end*/
  /*%if options.examCount != null*/
  AND t1.exam_count = /*options.examCount*/1
  /*%end*/
ORDER BY
  t1.created_at DESC
LIMIT /*pageable.getPageSize()*/20
OFFSET /*pageable.getOffset()*/0;
