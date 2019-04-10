package jp.ne.kuma.exam.persistence.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.seasar.doma.Entity;
import org.seasar.doma.jdbc.entity.NamingType;

import lombok.Data;

@Data
@Entity(naming = NamingType.SNAKE_LOWER_CASE)
public class RedmineUser implements Serializable {

  private static final long serialVersionUID = -2406026307330624962L;

  private Integer id;
  private String login;
  private String hashedPassword;
  private String firstname;
  private String lastname;
  private boolean admin;
  private int status;
  private LocalDateTime createdOn;
  private LocalDateTime updatedOn;
  private String type;
  private String salt;
  private boolean mustChangePasswd;
  private LocalDateTime passwdChangedOn;
}
