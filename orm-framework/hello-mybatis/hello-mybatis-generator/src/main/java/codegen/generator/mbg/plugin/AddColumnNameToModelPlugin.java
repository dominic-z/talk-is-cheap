package codegen.generator.mbg.plugin;

import lombok.val;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;

import java.util.List;

public class AddColumnNameToModelPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * 给model类添加一些用来标识列名的静态常量，这些列名可以用来作为orderByClause的参数
     * @param topLevelClass
     *            the generated base record class
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            val actualColumnName = column.getActualColumnName();
            val field = new Field(actualColumnName.toUpperCase(), new FullyQualifiedJavaType("String"));
            field.setFinal(true);
            field.setStatic(true);
            field.setVisibility(JavaVisibility.PUBLIC);
            field.setInitializationString(String.format("\"%s\"", actualColumnName));
            topLevelClass.addField(field);
        }

        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }
}
