SELECT
  COUNT(*)
FROM
  answers
WHERE
  (exam_no, question_no)
  IN (
  /*%for question: data */
    (
    /* question.examNo */1
    , /* question.questionNo */22
    )
    /*%if question_has_next */
      /*# "," */
    /*%end*/
  /*%end*/
  )
  AND deleted IS NOT TRUE;
