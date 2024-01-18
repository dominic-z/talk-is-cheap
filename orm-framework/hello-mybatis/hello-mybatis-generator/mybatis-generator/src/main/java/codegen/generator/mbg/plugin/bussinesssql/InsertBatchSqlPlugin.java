package codegen.generator.mbg.plugin.bussinesssql;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.DefaultXmlFormatter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.PrimitiveTypeWrapper;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author dominiczhu
 * @version 1.0
 * @title CustomizedSqlPlugin
 * @date 2022/1/11 5:32 下午
 */
public class InsertBatchSqlPlugin extends PluginAdapter {

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {

        sqlMapInsertBatch(document, introspectedTable);

        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {


        clientInsertBatch(interfaze, introspectedTable);

        return true;
    }

    private void clientInsertBatch(Interface interfaze, IntrospectedTable introspectedTable) {
        Method insertBatch = new Method("insertBatch");
        context.getCommentGenerator().addGeneralMethodComment(insertBatch, introspectedTable);
        insertBatch.setAbstract(true);
        insertBatch.setReturnType(PrimitiveTypeWrapper.getIntInstance());
        final FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(
                String.format("Collection<%s>", introspectedTable.getFullyQualifiedTable().getDomainObjectName()));
        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.Collection"));
        Parameter insertBatchParameter = new Parameter(parameterType, "records");

        insertBatch.addParameter(insertBatchParameter);
        interfaze.addMethod(insertBatch);
    }


    private void sqlMapInsertBatch(Document document, IntrospectedTable introspectedTable) {
        // insertBatch

        final XmlElement rootElement = document.getRootElement();
        final XmlElement insertBatch = new XmlElement("insert");
        context.getCommentGenerator().addComment(insertBatch);

        insertBatch.addAttribute(new Attribute("id", "insertBatch"));
        insertBatch.addAttribute(new Attribute("parameterType", "java.util.Collection"));
        insertBatch.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));

        final Set<String> primaryKeyNames = new HashSet<>();
        if (introspectedTable.getPrimaryKeyColumns() != null) {
            introspectedTable.getPrimaryKeyColumns().forEach(c -> primaryKeyNames.add(c.getActualColumnName()));
        }

        final List<String> columnNames = new ArrayList<>();
        final List<String> valueFields = new ArrayList<>();
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            final String actualColumnName = column.getActualColumnName();
            final String javaProperty = column.getJavaProperty();
            final String jdbcTypeName = column.getJdbcTypeName();

            if (!primaryKeyNames.contains(actualColumnName)) {
                columnNames.add(actualColumnName);
                valueFields.add(String.format("#{item.%s,jdbcType=%s}", javaProperty, jdbcTypeName));
            }
        }

        // 添加列名
        insertBatch.addElement(new TextElement(String.format("(%s)", String.join(",", columnNames))));
        insertBatch.addElement(new TextElement("values"));

        final XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("collection", "collection"));
        foreach.addAttribute(new Attribute("item", "item"));
        foreach.addAttribute(new Attribute("separator", ","));

        foreach.addElement(new TextElement(String.format("(%s)", String.join(",", valueFields))));

        insertBatch.addElement(foreach);
        rootElement.addElement(insertBatch);
    }

}
