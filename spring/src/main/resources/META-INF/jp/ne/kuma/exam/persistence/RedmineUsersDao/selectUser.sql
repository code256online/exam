SELECT
  id
  , login
  , hashed_password
  , firstname
  , lastname
  , admin
  , status
  , created_on
  , updated_on
  , type
  , salt
  , must_change_passwd
  , passwd_changed_on
FROM
  bitnami_redmine.users
WHERE
  login = /*username*/'y.maikuma'
  AND must_change_passwd IS NOT TRUE
  AND status = 1;
