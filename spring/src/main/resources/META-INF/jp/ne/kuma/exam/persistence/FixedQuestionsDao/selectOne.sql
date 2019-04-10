SELECT
  id
  , name
  , questions
  , modified_at
FROM
  fixed_questions
WHERE
  id = /*id*/1
  /*%if modifiedAt != null*/
  AND modified_at = /*modifiedAt*/'2019/01/14/19:02:50'
  /*%end*/
  AND deleted IS NOT TRUE;
