package cronapi.conversion;

import cronapi.Var;
import cronapi.i18n.Messages;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Conversão")
class OperationsTest {

  private TimeZone oldTimeZone;

  @Test
  @DisplayName("Texto para base64")
  void stringToBase64() throws Exception {
    assertThat(Operations.StringToBase64(Var.VAR_NULL)).isEqualTo(Var.valueOf(""));
    assertThat(Operations.StringToBase64(Var.valueOf("Lorem ipsum dolor sit amet, consectetur adipiscing elit."))).isEqualTo(Var.valueOf("TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdC4="));
  }

  @Test
  @DisplayName("Base64 para texto")
  void base64ToString() throws Exception {
    assertThat(Operations.base64ToString(Var.valueOf("TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdC4="))).isEqualTo(Var.valueOf("Lorem ipsum dolor sit amet, consectetur adipiscing elit."));
  }

  @SuppressWarnings("rawtypes")
  @Test
  void convert() {
    var testDate1 = Var.valueOf("2012-05-12T04:05:24Z").getObjectAsDateTime();
    var testDate2 = Calendar.getInstance();
    testDate2.setTime(testDate1.getTime());
    testDate2.set(Calendar.HOUR_OF_DAY, 0);
    testDate2.set(Calendar.MINUTE, 0);
    testDate2.set(Calendar.SECOND, 0);
    testDate2.set(Calendar.MILLISECOND, 0);

    assertThat(Operations.convert(Var.valueOf(1), Var.valueOf("STRING")).getObject()).isEqualTo("1");
    assertThat(Operations.convert(Var.valueOf(true), Var.valueOf("STRING")).getObject()).isEqualTo("true");
    assertThat(Operations.convert(Var.valueOf("true"), Var.valueOf("BOOLEAN")).getObject()).isEqualTo(true);
    assertThat(Operations.convert(Var.valueOf("false"), Var.valueOf("BOOLEAN")).getObject()).isEqualTo(false);
    assertThat(Operations.convert(Var.valueOf("2012-05-12T04:05:24Z"), Var.valueOf("DATETIME")).getObject()).isEqualTo(testDate1);

    // SHORT ASSERTION
    assertThat(Operations.convert(Var.valueOf(1), Var.valueOf("SHORT")).getObject()).isEqualTo((short) 1);
    assertThat(Operations.convert(Var.valueOf(1L), Var.valueOf("SHORT")).getObject()).isEqualTo( (short) 1);
    assertThat(Operations.convert(Var.valueOf("1"), Var.valueOf("SHORT")).getObject()).isEqualTo( (short) 1);
    assertThat(Operations.convert(Var.valueOf('1'), Var.valueOf("SHORT")).getObject()).isEqualTo( (short) 1);
    assertThat(Operations.convert(Var.valueOf(1.0), Var.valueOf("SHORT")).getObject()).isEqualTo( (short) 1);
    assertThat(Operations.convert(Var.valueOf("1.0"), Var.valueOf("SHORT")).getObject()).isEqualTo( (short) 1);
    assertThat(Operations.convert(Var.valueOf(true), Var.valueOf("SHORT")).getObject()).isEqualTo( (short) 1);
    assertThat(Operations.convert(Var.valueOf(false), Var.valueOf("SHORT")).getObject()).isEqualTo( (short) 0);
    assertThat(Operations.convert(Var.valueOf("true"), Var.valueOf("SHORT")).getObject()).isEqualTo( (short) 1);
    assertThat(Operations.convert(Var.valueOf(" true "), Var.valueOf("SHORT")).getObject()).isEqualTo( (short) 1);
    assertThat(Operations.convert(Var.valueOf("false"), Var.valueOf("SHORT")).getObject()).isEqualTo( (short) 0);
    assertThat(Operations.convert(Var.valueOf(null), Var.valueOf("SHORT")).getObject()).isEqualTo( (short) 0);
    assertThat(Operations.convert(Var.valueOf("null"), Var.valueOf("SHORT")).getObject()).isEqualTo( (short) 0);


    assertThat(Operations.convert(Var.valueOf("2012-05-12T04:05:24Z"), Var.valueOf("DATE")).getObject()).isEqualTo(testDate2);
    assertThat(Operations.convert(Var.valueOf("2012-05-12T04:05:24Z"), Var.valueOf("TEXTTIME")).getObject()).isEqualTo("04:05:24");
    assertThat(Operations.convert(Var.valueOf("2012-05-12T04:05:24Z"), Var.valueOf("TIME")).getObject()).isEqualTo(Var.valueOf("1970-01-01T04:05:24Z").getObjectAsDateTime());
    assertThat(Operations.convert(Var.valueOf("2012-05-12T04:05:24Z"), Var.valueOf("ISODATE")).getObject()).isEqualTo("2012-05-12T04:05:24Z");

    assertThat(Operations.convert(Var.valueOf("1"), Var.valueOf("INTEGER")).getObject()).isEqualTo(1L);
    assertThat(Operations.convert(Var.valueOf("1.1"), Var.valueOf("INTEGER")).getObject()).isEqualTo(1L);
    assertThat(Operations.convert(Var.valueOf("1.1"), Var.valueOf("DOUBLE")).getObject()).isEqualTo(1.1D);
    assertThat(Operations.convert(Var.valueOf("1"), Var.valueOf("DOUBLE")).getObject()).isEqualTo(1.0D);

    Map<String, String> map = new LinkedHashMap<>();
    map.put("Test", "Value");

    assertThat(Operations.convert(Var.valueOf(map), Var.valueOf("MAP")).getObject()).isEqualTo(map);

    List<String> list = new LinkedList<>();
    list.add("Test");

    assertThat(((List) Operations.convert(Var.valueOf(list), Var.valueOf("LIST")).getObject()).get(0)).isEqualTo(list.get(0));

    assertThat(((List) Operations.convert(Var.valueOf("[1,2]"), Var.valueOf("LIST")).getObject()).get(0)).isEqualTo("1");
    assertThat(((List) Operations.convert(Var.valueOf("1"), Var.valueOf("LIST")).getObject()).get(0)).isEqualTo("1");

    byte[] test = "teste".getBytes();
    String b64 = new String(Base64.getEncoder().encode(test));

    assertThat(Operations.convert(Var.valueOf(test), Var.valueOf("BYTEARRAY")).getObject()).isEqualTo(test);
    assertThat(new String((byte[]) Operations.convert(Var.valueOf(b64), Var.valueOf("BYTEARRAY")).getObject())).isEqualTo(new String(test));
    assertThat(new String((byte[]) Operations.convert(Var.valueOf("teste"), Var.valueOf("BYTEARRAY")).getObject())).isEqualTo(new String(test));
  }

  @BeforeEach
  void setUp() {
    oldTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Messages.set(new Locale("pt", "BR"));
  }

  @AfterEach
  void tearDown() {
    TimeZone.setDefault(oldTimeZone);
  }
}