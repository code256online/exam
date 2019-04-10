SELECT
  /*%expand*/*
FROM
  answers
WHERE
  exam_no = /*examNo*/1
  AND question_no = /*questionNo*/1
  /*%if modifiedAt != null*/
  AND modified_at = /*modifiedAt*/'2018/01/01 12:34:56'
  /*%end*/
;
