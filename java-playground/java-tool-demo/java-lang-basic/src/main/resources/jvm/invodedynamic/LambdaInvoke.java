import java.util.stream.IntStream;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Lambda
 * @date 2021/8/19 上午11:19
 */
public class LambdaInvoke {

    public static void main(String[] args) {
        int x = 2;
        IntStream.of(1, 2, 3).map(i -> i * 2).map(i -> i * x);
    }

}
