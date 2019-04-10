SELECT
  id
  , name
  , questions
FROM
  fixed_questions
WHERE
  deleted IS NOT TRUE
ORDER BY
  id ASC;
