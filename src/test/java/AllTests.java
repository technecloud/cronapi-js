import br.com.cronapi.DatabaseTest;
import br.com.cronapi.RestClientTest;
import br.com.cronapi.VarTest;
import br.com.cronapi.conversion.ConversionTest;
import br.com.cronapi.dateTime.DateTimeTest;
import br.com.cronapi.list.ListTest;
import br.com.cronapi.rest.security.CronappSecurityTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ConversionTest.class, DateTimeTest.class, ListTest.class, CronappSecurityTest.class, DatabaseTest.class,
    RestClientTest.class, VarTest.class})
public class AllTests {
}
