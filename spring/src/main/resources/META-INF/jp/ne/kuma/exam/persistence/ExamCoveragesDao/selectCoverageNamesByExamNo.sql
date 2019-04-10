SELECT
  id,
  name
FROM
  exam_coverages
WHERE
  (
    exam_no = /*examNo*/1
    /*%if !includeDeleted*/
    OR exam_no = -1
    /*%end*/
  )
  /*%if !includeDeleted*/
  AND deleted IS NOT TRUE
  /*%end*/
ORDER BY
  id ASC;
