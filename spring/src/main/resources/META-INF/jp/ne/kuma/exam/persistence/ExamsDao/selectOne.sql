SELECT
  exam_no
  , exam_name
  , passing_score
  , deleted
  , created_at
  , modified_at
FROM
  exams
WHERE
  exam_no = /*examNo*/1
  /*%if modifiedAt != null*/
  AND modified_at = /*modifiedAt*/'2018/01/01 12:34:56'
  /*%end*/
;
