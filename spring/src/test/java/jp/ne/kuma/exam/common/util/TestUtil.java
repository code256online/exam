package jp.ne.kuma.exam.common.util;

import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.seasar.doma.Domain;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 単体テスト補助ユーティリティ。
 *
 * @author Mike
 */
public class TestUtil {

  /** エクセルローダーの唯一のインスタンス */
  public static final ExcelDataLoader excel = ExcelDataLoader.instance;

  /**
   * private コンストラクタ
   */
  private TestUtil() {
    super();
  }

  /**
   * テスト用データを java.io.File 形式で取得します。
   *
   * @param testClass
   *          テストクラス
   * @param fileName
   *          ファイル名
   * @return ファイル情報
   */
  public static File getDataFileAsFile(Class<?> testClass, String fileName) {
    return new File(computeFileName(testClass, fileName, null));
  }

  /**
   * テスト用データをバイト配列で取得します。
   *
   * @param testClass
   *          テストクラス
   * @param fileName
   *          ファイル名
   * @return バイト配列
   */
  public static byte[] getDataFileAsBytes(Class<?> testClass, String fileName) throws IOException {

    Path path = Paths.get(computeFileName(testClass, fileName, null));
    return Files.readAllBytes(path);
  }

  /**
   * ファイル名を算出。
   *
   * @param clazz
   *          テストクラス
   * @param fileName
   *          ファイル名
   * @return ディレクトリ表現
   */
  public static <T> String computeFileName(Class<T> clazz, String fileName, String ext) {

    return new StringBuilder("src/test/resources/")
        .append(clazz.getPackage().getName().replaceAll("\\.", "/"))
        .append("/")
        .append(clazz.getSimpleName())
        .append("Data/")
        .append(fileName)
        .append(ext != null ? ext : StringUtils.EMPTY)
        .toString();
  }

  /**
   * エクセルデータを読み込むクラス。
   *
   * @author Mike
   */
  public static class ExcelDataLoader {

    /** 唯一のインスタンス */
    private static final ExcelDataLoader instance = new ExcelDataLoader();

    /**
     * private コンストラクタ
     */
    private ExcelDataLoader() {
      super();
    }

    /**
     * 対象のエクセルのシートを POJO として読み込む。<br />
     * 複数行あっても最初の 1 行のみ返す。<br />
     * 先頭行を見出しとし、見出し名とフィールド名が一致する場合のみ値のセットを試みる。<br />
     * 非対応の型マッピングが必要な場合、#load にマッパーを渡して対処してください。
     *
     * @param beanType
     *          マッピングする型情報
     * @param testClass
     *          テストクラス
     * @param fileName
     *          ファイル名
     * @param sheetName
     *          シート名
     * @return マッピングした POJO
     */
    public <T> T loadAsPojo(Class<T> beanType, Class<?> testClass, String fileName, String sheetName) {
      return load(pojoMapper(beanType, loadHeader(testClass, fileName, sheetName), testClass, fileName), true,
          testClass, fileName, sheetName)
              .findFirst().orElse(null);
    }

    /**
     * 対象のエクセルのシートを POJO のリスト（1 行につき 1 インスタンス）として読み込む。
     * 先頭行を見出しとし、見出し名とフィールド名が一致する場合のみ値のセットを試みる。<br />
     * 非対応の型マッピングが必要な場合、#load にマッパーを渡して対処してください。
     *
     * @param beanType
     *          マッピングする型情報
     * @param testClass
     *          テストクラス
     * @param fileName
     *          ファイル名
     * @param sheetName
     *          シート名
     * @return マッピングした POJO
     */
    public <T> List<T> loadAsPojoList(Class<T> beanType, Class<?> testClass, String fileName, String sheetName) {
      return (List<T>) load(pojoMapper(beanType, loadHeader(testClass, fileName, sheetName), testClass, fileName), true,
          testClass, fileName, sheetName)
              .collect(Collectors.toList());
    }

    /**
     * 対象のエクセルのシートを 1 列目を key, 2 列目を value として、<br />
     * LinkedMultiValueMap で読み込む。<br />
     * MockMvc に渡すリクエストパラメータとして使用できる。<br />
     * ※先頭行も有効データとして読み込む。
     *
     * @param testClass
     *          テストクラス
     * @param fileName
     *          ファイル名
     * @param sheetName
     *          シート名
     * @return リクエストパラメータ用 LinkedMultiValueMap
     */
    public MultiValueMap<String, String> loadAsRequestParam(String delimiter, Class<?> testClass, String fileName,
        String sheetName) {
      return load(stringListMapper(), false, testClass, fileName, sheetName)
          .filter(x -> !x.isEmpty())
          .collect(Collectors.toMap(x -> x.get(0),
              x -> Arrays.asList(StringUtils.split(x.get(1), delimiter)),
              (x, y) -> y,
              LinkedMultiValueMap::new));
    }

    /**
     * 指定したエクセルデータシートのA列の文字列をKey、B列に記載されている名前のシートで<br />
     * {@link #loadAsPojoList(Class, Class, String, String)}を呼んだ結果のリストをValueとして、<br
     * />
     * Value が指定クラスのリスト形式になっている Map&lt;String, List&lt;T&gt;&gt; を返します。
     *
     * @param beanType
     *          マッピングする型情報
     * @param testClass
     *          試験実行クラス
     * @param bookName
     *          ブック名
     * @param sheetName
     *          シート名
     * @return T 型ののリストを Value に持つ Map
     */
    public <T> Map<String, List<T>> loadAsMappedPojoList(Class<T> beanType, Class<?> testClass, String bookName,
        String sheetName) {
      return load(stringListMapper(), true, testClass, bookName, sheetName)
          .collect(Collectors.toMap(x -> x.get(0),
              x -> loadAsPojoList(beanType, testClass, bookName, x.get(1)),
              (x, y) -> y,
              LinkedHashMap::new));
    }

    /**
     * ファイル読み込み。
     *
     * @param mapper
     *          マッパー
     * @param hasHeader
     *          見出し行があれば true （見出し行は読み込まない）
     * @param testClass
     *          テストクラス
     * @param fileName
     *          ファイル名
     * @param sheetName
     *          シート名
     * @return 行ごとに T 型にマッパーでマッピングした Stream
     */
    public <T> Stream<T> load(Function<Row, T> mapper, boolean hasHeader, Class<?> testClass, String fileName,
        String sheetName) {

      String file = computeFileName(testClass, fileName, ".xlsx");
      try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
        try (XSSFWorkbook book = new XSSFWorkbook(input)) {
          return StreamSupport.stream(book.getSheet(sheetName).spliterator(), false)
              .skip(hasHeader ? 1 : 0).map(mapper);
        }
      } catch (Exception e) {
        throw new IllegalStateException(file, e);
      }
    }

    /**
     * ファイル読み込み。
     *
     * @param mapper
     *          マッパー
     * @param testClass
     *          テストクラス
     * @param fileName
     *          ファイル名
     * @param sheetName
     *          シート名
     * @return 行ごとに T 型にマッパーでマッピングした Stream
     */
    public <T> Stream<T> load(Function<Row, T> mapper, Class<?> testClass, String fileName, String sheetName) {
      return load(mapper, true, testClass, fileName, sheetName);
    }

    /**
     * セルの値を文字列として取得。
     *
     * @param cell
     *          セル
     * @return セルの値
     */
    @SuppressWarnings("deprecation")
    public String asString(Cell cell) {

      if (cell == null) {
        return null;
      }

      switch (cell.getCellType()) {
      case Cell.CELL_TYPE_STRING:
        return cell.getStringCellValue();
      case Cell.CELL_TYPE_BLANK:
        return StringUtils.EMPTY;
      case Cell.CELL_TYPE_NUMERIC:
        return String.valueOf(cell.getNumericCellValue());
      case Cell.CELL_TYPE_FORMULA:
        try {
          return cell.getStringCellValue();
        } catch (IllegalStateException ise) {
          return String.valueOf(Math.round(cell.getNumericCellValue()));
        }
      case Cell.CELL_TYPE_BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
      default:
        throw new IllegalArgumentException(cell.getCellTypeEnum().toString());
      }
    }

    /**
     * セルの値を double として取得。
     *
     * @param cell
     *          セル
     * @return セルの値
     */
    @SuppressWarnings("deprecation")
    public double asDouble(Cell cell) {

      if (cell == null) {
        return 0;
      }

      int type = cell.getCellType();
      if (Cell.CELL_TYPE_NUMERIC == type || Cell.CELL_TYPE_FORMULA == type) {
        return cell.getNumericCellValue();
      } else if (Cell.CELL_TYPE_STRING == type) {
        return Double.valueOf(asString(cell));
      } else if (Cell.CELL_TYPE_BLANK == type) {
        return 0;
      }

      throw new IllegalArgumentException(cell.getCellTypeEnum().toString());
    }

    /**
     * セルの値を long として取得。
     *
     * @param cell
     *          セル
     * @return セルの値
     */
    public long asLong(Cell cell) {
      return Math.round(asDouble(cell));
    }

    /**
     * セルの値を BigDecimal として取得。
     *
     * @param cell
     *          セル
     * @return セルの値
     */
    public BigDecimal asDecimal(Cell cell) {

      if (cell == null) {
        return null;
      }
      String value = asString(cell);
      if (StringUtils.isBlank(value)) {
        return null;
      }

      return new BigDecimal(value);
    }

    /**
     * セルの値を LocalTime として取得。
     *
     * @param cell
     *          セル
     * @return セルの値
     */
    public LocalTime asLocalTime(Cell cell) {

      if (cell == null) {
        return null;
      }

      return asLocalDateTime(cell).toLocalTime();
    }

    /**
     * セルの値を LocalDate として取得。
     *
     * @param cell
     *          セル
     * @return セルの値
     */
    public LocalDate asLocalDate(Cell cell) {

      if (cell == null) {
        return null;
      }

      return asLocalDateTime(cell).toLocalDate();
    }

    /**
     * セルの値を LocalDateTime として取得。
     *
     * @param cell
     *          セル
     * @return セルの値
     */
    @SuppressWarnings("deprecation")
    public LocalDateTime asLocalDateTime(Cell cell) {

      if (cell == null) {
        return null;
      }

      int type = cell.getCellType();
      if (Cell.CELL_TYPE_NUMERIC == type) {
        if (DateUtil.isCellDateFormatted(cell)) {
          return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
      } else if (Cell.CELL_TYPE_BLANK == type) {
        return LocalDateTime.now();
      } else if (Cell.CELL_TYPE_STRING == type) {
        return LocalDateTime.parse(asString(cell));
      }

      throw new IllegalArgumentException(cell.getCellTypeEnum().toString());
    }

    /**
     * セルの値を真偽値として取得。
     *
     * @param cell
     *          セル
     * @return セルの値
     */
    @SuppressWarnings("deprecation")
    public boolean asBoolean(Cell cell) {

      if (cell == null) {
        return false;
      }

      if (Cell.CELL_TYPE_BOOLEAN == cell.getCellType()) {
        return cell.getBooleanCellValue();
      } else if (Cell.CELL_TYPE_STRING == cell.getCellType()) {
        return Boolean.parseBoolean(asString(cell));
      } else if (Cell.CELL_TYPE_BLANK == cell.getCellType()) {
        return false;
      }

      throw new IllegalArgumentException(cell.getCellTypeEnum().toString());
    }

    /**
     * セルの値を Stream として取得。
     *
     * @param cell
     *          セル
     * @return セルの値
     */
    @SuppressWarnings("deprecation")
    public <T> Stream<T> asStream(Cell cell, Class<T> type) {

      if (cell == null) {
        return null;
      }

      if (Cell.CELL_TYPE_BLANK == cell.getCellType()) {
        return new ArrayList<T>().stream();
      }

      return getValues(type, asString(cell).split(";"));
    }

    /**
     * セルの値を List として取得。<br />
     * 対応していない型の場合は空のリストが返される。
     *
     * @param cell
     *          セル
     * @return セルの値
     */
    public <T> List<T> asList(Cell cell, Class<T> type) {

      if (cell == null) {
        return null;
      }

      return asStream(cell, type).collect(Collectors.toList());
    }

    /**
     * セルの値を文字列の Map として取得。
     *
     * @param map
     *          追加対象
     * @param cell
     *          セル
     * @param delimiter
     *          Key と Value の区切り文字
     * @return セルの値
     */
    public Map<String, String> asStringMap(Map<String, String> map, Cell cell, String delimiter) {

      if (cell == null) {
        return null;
      }

      String[] cellValue = asString(cell).split(delimiter);
      map.put(cellValue[0], cellValue[1]);
      return map;
    }

    /**
     * ヘッダ行の文字列リストを取得。
     *
     * @param testClass
     *          テストクラス
     * @param fileName
     *          ファイル名
     * @param sheetName
     *          シート名
     * @return 見出しのリスト
     */
    public List<String> loadHeader(Class<?> testClass, String fileName, String sheetName) {
      return load(stringListMapper(), false, testClass, fileName, sheetName).findFirst().orElse(new ArrayList<>());
    }

    /**
     * 読み込んだデータを POJO にマッピングするための汎用マッパー。
     *
     * @param beanType
     *          マッピングする型情報
     * @param fieldNames
     *          プロパティ名のリスト
     * @return マッパー
     */
    public <T> Function<Row, T> pojoMapper(Class<T> beanType, List<String> fieldNames, Class<?> testClass,
        String filename) {

      return x -> {
        try {
          T ret = beanType.getDeclaredConstructor().newInstance();
          for (Class<?> type = beanType; type != null && !Object.class.equals(type); type = type.getSuperclass()) {
            for (Field f : type.getDeclaredFields()) {
              if (fieldNames.contains(f.getName())) {

                PropertyDescriptor descriptor = new PropertyDescriptor(f.getName(), beanType);
                Object value = getCellValue(x.getCell(fieldNames.indexOf(f.getName())), f, testClass, filename);
                if (value != null) {
                  descriptor.getWriteMethod().invoke(ret, value);
                }
              }
            }
          }
          return ret;
        } catch (Exception e) {
          throw new IllegalStateException(e.getMessage(), e);
        }
      };
    }

    /**
     * 文字列リスト専用マッパー。
     *
     * @return マッパー
     */
    public Function<Row, List<String>> stringListMapper() {
      return x -> StreamSupport.stream(x.spliterator(), false)
          .map(this::asString)
          .collect(Collectors.toList());
    }

    /**
     * セルの値を文字列の Stream として返すマッパー。
     *
     * @return マッパー
     */
    public Function<Row, Stream<String>> stringStreamMapper() {
      return x -> StreamSupport.stream(x.spliterator(), false).map(this::asString);
    }

    /**
     * 文字列 Map 専用マッパー。
     *
     * @param delimiter
     *          Key と Value の区切り文字
     * @return マッパー
     */
    public Function<Row, Map<String, String>> stringMapMapper(String delimiter) {
      return x -> {
        Map<String, String> ret = new LinkedHashMap<>();
        StreamSupport.stream(x.spliterator(), false)
            .forEachOrdered(cell -> asStringMap(ret, cell, delimiter));
        return ret;
      };
    }

    /**
     * フィールドの型情報に応じてセルの値を取得。
     *
     * @param cell
     *          セル
     * @param f
     *          フィールド
     * @return 取得した値
     */
    private Object getCellValue(Cell cell, Field f, Class<?> testClass, String filename) {

      Domain domain = f.getType().getAnnotation(Domain.class);
      if (domain != null) {
        Object value = getCellValue(cell, domain.valueType(), null, testClass, filename);
        try {
          return f.getType().getDeclaredMethod(domain.factoryMethod(), domain.valueType()).invoke(null, value);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
          return null;
        }
      } else {
        return getCellValue(cell, f.getType(), f.getGenericType(), testClass, filename);
      }
    }

    /**
     * 型情報に応じてセルの値を取得。
     *
     * @param cell
     *          セル
     * @param type
     *          型
     * @param genericType
     *          ジェネリック型
     * @return 取得した値
     */
    private Object getCellValue(Cell cell, Class<?> type, Type genericType, Class<?> testClass, String filename) {

      Sheet sheet = null;
      String sheetName = asString(cell);
      if (sheetName != null) {
        sheet = cell.getSheet().getWorkbook().getSheet(sheetName);
      }
      if (sheet != null) {
        if (List.class.equals(type)) {
          try {
            Class<?> target = Class
                .forName(((ParameterizedType) genericType).getActualTypeArguments()[0].getTypeName());
            return loadAsPojoList(target, testClass, filename, sheet.getSheetName());
          } catch (ClassNotFoundException e) {
            return null;
          }
        } else {
          return this.loadAsPojo(type, testClass, filename, sheet.getSheetName());
        }
      }
      if (StringUtils.equals(asString(cell), "${null}")) {
        return null;
      }
      if (String.class.equals(type)) {
        return asString(cell);
      }
      if (BigDecimal.class.equals(type)) {
        return asDecimal(cell);
      }
      if (Double.class.equals(type) || double.class.equals(type)) {
        return asDouble(cell);
      }
      if (Long.class.equals(type) || long.class.equals(type)) {
        return asLong(cell);
      }
      if (Integer.class.equals(type) || int.class.equals(type)) {
        return (int) asLong(cell);
      }
      if (LocalTime.class.equals(type)) {
        return asLocalTime(cell);
      }
      if (LocalDate.class.equals(type)) {
        return asLocalDate(cell);
      }
      if (LocalDateTime.class.equals(type)) {
        return asLocalDateTime(cell);
      }
      if (Boolean.class.equals(type) || boolean.class.equals(type)) {
        return asBoolean(cell);
      }
      if (type.isEnum()) {
        String name = asString(cell);
        return Arrays.stream(type.getEnumConstants()).parallel()
            .filter(x -> ((Enum<?>) x).toString().equals(name))
            .findFirst().orElse(null);
      }
      if (type.isArray()) {
        List<?> list = asStream(cell, type.getComponentType()).collect(Collectors.toList());
        return list.toArray((Object[]) java.lang.reflect.Array.newInstance(type.getComponentType(), list.size()));
      }
      if (List.class.equals(type)) {
        if (genericType instanceof ParameterizedType) {
          Type[] genericTypes = ((ParameterizedType) genericType).getActualTypeArguments();
          try {
            Class<?> domainType = Class.forName(genericTypes[0].getTypeName());
            Domain domain = domainType.getAnnotation(Domain.class);
            if (domain != null) {
              List<Object> values = asStream(cell, domain.valueType()).collect(Collectors.toList());
              List<Object> ret = new ArrayList<>();
              for (Object value : values) {
                try {
                  ret.add(domainType.getDeclaredMethod(domain.factoryMethod(), domain.valueType()).invoke(null, value));
                } catch (ReflectiveOperationException e) {
                  return null;
                }
              }
              return ret;
            } else {
              return asStream(cell, Class.forName(genericTypes[0].getTypeName())).collect(Collectors.toList());
            }
          } catch (ClassNotFoundException e) {
            return null;
          }
        }
      }

      return null;
    }

    /**
     * 文字列の配列を指定の型にパースした Stream を返す。
     *
     * @param type
     *          パースする型
     * @param values
     *          文字列の配列
     * @return それぞれパースした結果のStream
     */
    @SuppressWarnings("unchecked")
    private <T> Stream<T> getValues(Class<T> type, String... values) {

      if (String.class.equals(type)) {
        return (Stream<T>) Stream.of(values);
      }

      if (BigDecimal.class.equals(type)) {
        return (Stream<T>) Stream.of(values).map(BigDecimal::new);
      }

      if (Double.class.equals(type) || double.class.equals(type)) {
        return (Stream<T>) Stream.of(values).map(Double::parseDouble);
      }

      if (Long.class.equals(type) || long.class.equals(type)) {
        return (Stream<T>) Stream.of(values).map(x -> Math.round(Double.parseDouble(x)));
      }

      if (Integer.class.equals(type) || int.class.equals(type)) {
        return (Stream<T>) Stream.of(values).map(x -> (int) Math.round(Double.parseDouble(x)));
      }

      if (LocalDate.class.equals(type)) {
        return (Stream<T>) Stream.of(values).map(LocalDate::parse);
      }

      if (LocalTime.class.equals(type)) {
        return (Stream<T>) Stream.of(values).map(LocalTime::parse);
      }

      if (LocalDateTime.class.equals(type)) {
        return (Stream<T>) Stream.of(values).map(LocalDateTime::parse);
      }

      if (Boolean.class.equals(type) || boolean.class.equals(type)) {
        return (Stream<T>) Stream.of(values).map(Boolean::parseBoolean);
      }

      if (type.isEnum()) {
        return (Stream<T>) Stream.of(values)
            .map(x -> Arrays.stream(type.getEnumConstants()).parallel()
                .filter(y -> ((Enum<?>) y).toString().equals(x))
                .findFirst().orElse(null));
      }

      return new ArrayList<T>().stream();
    }
  }
}
