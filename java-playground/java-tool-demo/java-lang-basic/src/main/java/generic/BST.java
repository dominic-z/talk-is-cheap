package generic;

/**
 * @author dominiczhu
 * @version 1.0
 * @title BST
 * @date 2021/11/13 上午11:10
 */
public class BST<K extends Comparable<K>, V> {
    Node<K, V> node;

    public void show() {

        System.out.println(node.left);
        System.out.println(node.right);
    }

}

class Node<K extends Comparable<K>, V> {
    K key;
    V value;
    int N;

    Node<K, V> left;
    Node<K, V> right;

    Node(Node<K, V> left, Node<K, V> right, int N) {
        this.left = left;
        this.right = right;
        this.N = N;

    }

}
