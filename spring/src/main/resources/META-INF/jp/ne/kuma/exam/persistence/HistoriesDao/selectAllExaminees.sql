SELECT DISTINCT
  t2.id AS examinee_id
  , CONCAT(t2.lastname, ' ', t2.firstname) AS examinee_name
FROM
  histories t1
  INNER JOIN bitnami_redmine.users t2
    ON t2.id = t1.examinee_id
    AND t2.status IS TRUE
ORDER BY
  t2.id;
