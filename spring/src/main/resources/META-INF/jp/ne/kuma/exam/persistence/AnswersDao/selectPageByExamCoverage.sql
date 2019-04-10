SELECT
  exam_no
  , question_no
  , exam_coverage
  , choices_count
  , correct_answers
  , deleted
  , created_at
FROM
  answers
WHERE
  exam_no = /*examNo*/1
/*%if examCoverage >= 0*/
  AND exam_coverage= /*examCoverage*/0
/*%end*/
  AND deleted IS NOT TRUE
ORDER BY
  question_no
LIMIT
  1
OFFSET
  /*offset*/0;
