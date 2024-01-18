package codegen;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Main
 * @date 2021/9/18 下午3:59
 */
public class CodeGeneratorMain {

    public static void main(String[] args) throws Exception {
        final CodeGeneratorManager codeGeneratorManager = new CodeGeneratorManager();
        codeGeneratorManager.genCode();
    }

}
