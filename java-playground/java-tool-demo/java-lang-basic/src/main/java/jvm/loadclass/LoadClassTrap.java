package jvm.loadclass;

/**
 * @author dominiczhu
 * @version 1.0
 * @title LoadClassTrap
 * @date 2021/9/13 下午8:26
 */
public class LoadClassTrap {

    public static void main(String[] args) {
        SingleTon singleTon = SingleTon.getInstance();
        System.out.println("count1=" + singleTon.count1);
        System.out.println("count2=" + singleTon.count2);

        System.out.println("爸爸的岁数:" + Son.factor);	//入口

    }
}


class SingleTon {
    private static SingleTon singleTon = new SingleTon(); // 将这一行挪到count2下一行
    public static int count1;
    public static int count2 = 0;
    public static Integer count3 = 0;


    private SingleTon() {
        count1++;
        count2++;
//        count3++会报错 因为准备阶段，count3是null，而new SingleTon是先于count3=0执行的，因此此时使用count3++就相当于对null使用++
//        count3++;
    }

    public static SingleTon getInstance() {
        return singleTon;
    }
}



class Grandpa
{
    static
    {
        System.out.println("爷爷在静态代码块");
    }
}
class Father extends Grandpa
{
    static
    {
        System.out.println("爸爸在静态代码块");
    }

    public static int factor = 25;

    public Father()
    {
        System.out.println("我是爸爸~");
    }
}
class Son extends Father
{
    static
    {
        System.out.println("儿子在静态代码块");
    }

    public Son()
    {
        System.out.println("我是儿子~");
    }
}