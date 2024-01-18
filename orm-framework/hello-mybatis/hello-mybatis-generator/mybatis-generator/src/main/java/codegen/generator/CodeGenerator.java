package codegen.generator;

import codegen.model.TableInfo;

/**
 * 主要逻辑接口
 */
public interface CodeGenerator {

    /**
     * 代码生成主要逻辑
     *
     * @param tableInfo
     */
    boolean genCode(TableInfo tableInfo);
}
