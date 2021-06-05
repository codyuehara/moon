import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MoonTest {

    @Test
    void testHello(){
        MoonCalculator test = new MoonCalculator();
        Assertions.assertEquals(test.sidereal(), 0.0);
    }
}
