package jp.ne.kuma.exam.common.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omg.CORBA.SystemException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * プロパティユーティリティ。
 *
 * @author Mike
 *
 */
@Component
public class PropertiesUtil {

  private PropertiesUtil() {
    super();
  }

  /**
   * ソースインスタンスの同名プロパティをコピーしたターゲットクラスの新しいインスタンスを返却します。
   *
   * @param targetClass
   *          インスタンスを生成するターゲットクラス
   * @param source
   *          プロパティのコピー元インスタンス
   * @return プロパティコピー済みの新しいインスタンス
   * @throws SystemException
   *           インスタンス生成に失敗した場合
   */
  public <T> T copyProperties(Class<T> targetClass, Object source) {
    try {
      T target = targetClass.newInstance();
      BeanUtils.copyProperties(source, target);
      return target;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new IllegalStateException();
    }
  }

  /**
   * ソースインスタンスの同名プロパティをコピーしたターゲットクラスの新しいインスタンスをリストで返却します。
   *
   * @param targetClass
   *          インスタンスを生成するターゲットクラス
   * @param source
   *          プロパティのコピー元インスタンス
   * @return プロパティコピー済みの新しいインスタンスのリスト
   * @throws インスタンス生成に失敗した場合
   */
  public <T> List<T> copyProperties(Class<T> targetClass, List<?> source) throws SystemException {
    return source.stream().map(x -> copyProperties(targetClass, x)).collect(Collectors.toList());
  }

  /**
   * ソースインスタンスの同名プロパティをコピーしたターゲットクラスの新しいインスタンスをリストで返却します。
   *
   * @param targetClass
   *          インスタンスを生成するターゲットクラス
   * @param sources
   *          プロパティのコピー元インスタンス（可変長）
   * @return プロパティコピー済みの新しいインスタンスのリスト
   * @throws インスタンス生成に失敗した場合
   */
  public <T> List<T> copyProperties(Class<T> targetClass, Object... sources) throws SystemException {
    return Stream.of(sources).map(x -> copyProperties(targetClass, x)).collect(Collectors.toList());
  }
}
