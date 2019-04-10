package jp.ne.kuma.exam.common.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
@ConfigurationProperties("exam.question.image")
public class ImageProperties {

  private String srcPattern;
  private String numberFormat;
}
