package jp.ne.kuma.exam.common.filter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RedmineUserPasswordEncoder implements PasswordEncoder {

  private static final String ALGORITHM = "SHA1";
  private static final Charset CHARSET = StandardCharsets.UTF_8;

  @Override
  public String encode(CharSequence rawPassword) {

    try {
      byte[] bytes = MessageDigest.getInstance(ALGORITHM).digest(((String) rawPassword).getBytes(CHARSET));
      return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    } catch (NoSuchAlgorithmException e) {
      return (String) rawPassword;
    }
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {

    String[] pass = StringUtils.split(encodedPassword);
    if (pass == null || pass.length < 2) {
      throw new AuthenticationCredentialsNotFoundException("ユーザーが存在しません。");
    }

    try {

      MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
      byte[] bytes1 = digest.digest(((String) rawPassword).getBytes(CHARSET));
      byte[] bytes2 = digest
          .digest((pass[1] + DatatypeConverter.printHexBinary(bytes1).toLowerCase()).getBytes(CHARSET));

      boolean result = StringUtils.equals(pass[0], DatatypeConverter.printHexBinary(bytes2).toLowerCase());

      if (result) {
        return result;
      }

      throw new AuthenticationCredentialsNotFoundException("パスワードが違います。");

    } catch (NoSuchAlgorithmException e) {

      return false;
    }
  }
}
