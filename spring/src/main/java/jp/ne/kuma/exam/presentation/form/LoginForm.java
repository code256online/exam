package jp.ne.kuma.exam.presentation.form;

import java.io.Serializable;

import lombok.Data;

/**
 * 認証情報
 *
 * @author Mike
 */
@Data
public class LoginForm implements Serializable {

  private static final long serialVersionUID = -7392049545750277336L;

  private String username;
  private String password;
}
