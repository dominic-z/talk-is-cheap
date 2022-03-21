import lombok.ToString;

/**
 * @author dominiczhu
 * @date 2020/9/19 9:14 上午
 */
@ToString
public class ToStringDemo {
    private String name="name";

    public static void main(String[] args) {
        System.out.println(new ToStringDemo());
    }
}
