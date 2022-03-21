import com.beust.jcommander.JCommander;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dominiczhu
 * @date 2020/9/19 8:42 上午
 */
public class OverviewDemo {
    @Test
    public void test(){
        Args args = new Args();
        String[] argv = { "-log", "2", "-groups", "unit" };
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);
        System.out.println(args);;

    }
}
