SELECT
  exam_no
  , exam_name
FROM
  exams
WHERE
  deleted IS NOT TRUE
ORDER BY
  exam_no ASC;
