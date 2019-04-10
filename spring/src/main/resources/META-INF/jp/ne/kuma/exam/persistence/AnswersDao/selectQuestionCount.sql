SELECT
  COUNT(*)
FROM
  answers
WHERE
  exam_no = /*examNo*/1
/*%if examCoverage >= 0*/
  AND exam_coverage = /*examCoverage*/0
/*%end*/
  AND deleted IS NOT TRUE;
