package polymorphic.domain;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Apple
 * @date 2021/8/13 下午3:21
 */
public class Apple extends Fruit {

    public int name = 1;

    // 重写方法的时候，子类方法的返回类型可以是父类方法返回类型的子类
    @Override
    public String get() {
        return String.valueOf(this.name);
    }

    private void showSupper() {

        // 如果访问同名字段，即只用name的话，那么子类字段会将父类字段隐藏掉
        System.out.println(super.name);
        System.out.println(this.name);
    }

    public static void main(String[] args) {
        System.out.println(new Apple());
        new Apple().showSupper();
    }
}
