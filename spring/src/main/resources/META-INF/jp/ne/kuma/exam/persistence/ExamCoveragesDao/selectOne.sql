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
  /*%if modifiedAt != null*/
  AND modified_at = /*modifiedAt*/'2018/01/01 12:34:56'
  /*%end*/
