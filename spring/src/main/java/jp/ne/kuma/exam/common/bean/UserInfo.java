package jp.ne.kuma.exam.common.bean;

import java.io.Serializable;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import lombok.Data;

@Data
@Component
@SessionScope
public class UserInfo implements Serializable {

  private static final long serialVersionUID = 4244404724850294965L;

  private Integer id;
  private boolean admin;

  public void clear() {

    this.id = null;
    this.admin = false;
  }
}
