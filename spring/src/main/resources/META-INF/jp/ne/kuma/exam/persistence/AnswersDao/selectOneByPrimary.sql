SELECT
  /*%expand*/*
FROM
  answers
WHERE
  exam_no = /*examNo*/1
  AND question_no = /*questionNo*/1
  AND deleted IS NOT TRUE;
