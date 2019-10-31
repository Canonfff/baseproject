import com.canon.start.ApplicationMain;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @program: baseproject
 * @Auther: canon
 * @Date: 2019/10/31 14:21
 * @Description:
 */
@SpringBootTest(classes = ApplicationMain.class)
@RunWith(SpringRunner.class)
public class ProjectTest {

    @Test
    public void baseTest() {
        System.err.println("spring-boot base test");
    }
}
