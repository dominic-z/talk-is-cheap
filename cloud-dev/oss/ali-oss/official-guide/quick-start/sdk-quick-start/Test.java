import java.util.Iterator;
import java.util.Map;
public class Test {
    public static  void readEnv(){
        System.out.println("OSS_ACCESS_KEY_ID="+System.getenv("OSS_ACCESS_KEY_ID"));
        System.out.println("OSS_ACCESS_KEY_SECRET="+System.getenv("OSS_ACCESS_KEY_SECRET"));

        Map<String, String> map = System.getenv();
        Iterator it = map.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry entry = (Map.Entry)it.next();
            System.out.print(entry.getKey()+"=");
            System.out.println(entry.getValue());
        }
    }
    public static void main(String[] args) {
        readEnv();
    }
}
