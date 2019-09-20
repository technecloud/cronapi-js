import br.com.cronapi.DatabaseTest;
import br.com.cronapi.RestClientTest;
import br.com.cronapi.VarTest;
import br.com.cronapi.dateTime.DateTimeTest;
import br.com.cronapi.json.JsonTest;
import br.com.cronapi.list.ListTest;
import br.com.cronapi.math.MathTest;
import br.com.cronapi.rest.security.CronappSecurityTest;
import br.com.cronapi.xml.XmlTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DateTimeTest.class, ListTest.class, CronappSecurityTest.class,
        DatabaseTest.class, RestClientTest.class, VarTest.class, JsonTest.class,
        XmlTest.class, MathTest.class})
public class AllTests {
}
