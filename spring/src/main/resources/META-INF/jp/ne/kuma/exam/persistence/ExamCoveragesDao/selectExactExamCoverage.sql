SELECT
  id
  , exam_no
  , name
  , deleted
  , created_at
  , modified_at
FROM
  exam_coverages
WHERE
  id = /*examCoverage*/1
  AND exam_no = /*examNo*/1
