SELECT
  correct_answers
FROM
  answers
WHERE
  (exam_no, question_no)
  IN (
  /*%for question : incorrects */
    (
    /* question.examNo */1
    , /* question.questionNo */22
    )
    /*%if question_has_next */
      /*# "," */
    /*%end*/
  /*%end*/
  )
  AND deleted IS NOT TRUE
ORDER BY
  FIELD(
    exam_no,
    /*%for question : incorrects */
      /* question.examNo */1
      /*%if question_has_next */
        /*# "," */
      /*%end*/
    /*%end*/
  ), FIELD(
    question_no,
    /*%for question : incorrects */
      /* question.questionNo */22
      /*%if question_has_next */
        /*# "," */
      /*%end */
    /*%end*/
  );
