package codegen.generator.mbg.plugin;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * @author dominiczhu
 * @version 1.0
 * @title CommentGenerator
 * @date 2022/1/12 11:22 上午
 */
public class CommentPlugin extends PluginAdapter {


    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        context.getCommentGenerator().addClassComment(topLevelClass, introspectedTable);

        topLevelClass.addJavaDocLine("/**");
        topLevelClass.addJavaDocLine(" * do not modify. if you want to add or delete some column, please re-run " +
                "codegen");
        topLevelClass.addJavaDocLine("*/");
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        topLevelClass.addJavaDocLine("/**");
        topLevelClass.addJavaDocLine(" * do not modify. if you want to add or delete some column, please re-run " +
                "codegen");
        topLevelClass.addJavaDocLine("*/");
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }


}
