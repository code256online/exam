SELECT
  count(*)
FROM
  answers
WHERE
  exam_no = /*examNo*/1
  AND deleted IS NOT TRUE;
