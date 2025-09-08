package codegen.generator.mbg.plugin.bussinesssql;

import org.apache.commons.lang3.StringUtils;
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
 * 创建批量insert的语句，依赖
 *
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
        sqlMapInsertBatchSelective(document,introspectedTable);

        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {


        clientInsertBatch(interfaze, introspectedTable);
        clientInsertBatchSelective(interfaze, introspectedTable);

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

    private void clientInsertBatchSelective(Interface interfaze, IntrospectedTable introspectedTable) {
        Method insertBatchSelective = new Method("insertBatchSelective");
        context.getCommentGenerator().addGeneralMethodComment(insertBatchSelective, introspectedTable);
        insertBatchSelective.setAbstract(true);
        insertBatchSelective.setReturnType(PrimitiveTypeWrapper.getIntInstance());
        final FullyQualifiedJavaType recordsType = new FullyQualifiedJavaType(
                String.format("Collection<%s>", introspectedTable.getFullyQualifiedTable().getDomainObjectName()));
        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.Collection"));
        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.HashSet"));
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));

        Parameter insertBatchParameter = new Parameter(recordsType, "records","@Param(\"records\")");
        Parameter excludeColNamesParameter = new Parameter(new FullyQualifiedJavaType("HashSet<String>"), "excludeColNames","@Param(\"excludeColNames\")");

        insertBatchSelective.addParameter(insertBatchParameter);
        insertBatchSelective.addParameter(excludeColNamesParameter);
        interfaze.addMethod(insertBatchSelective);
    }


    private void sqlMapInsertBatch(Document document, IntrospectedTable introspectedTable) {
        // insertBatch

        final XmlElement rootElement = document.getRootElement();
        final XmlElement insertBatch = new XmlElement("insert");
        context.getCommentGenerator().addComment(insertBatch);

        insertBatch.addAttribute(new Attribute("id", "insertBatch"));
        insertBatch.addAttribute(new Attribute("parameterType", "java.util.Collection"));
        insertBatch.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));


        final List<String> columnNames = new ArrayList<>(); // 组成insert into table (col1,col2) values ( ) 的col1,col2
        final List<String> valueFields = new ArrayList<>(); // 组成insert into table (col1,col2) values ( ) 的( )中间的部分
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            final String actualColumnName = column.getActualColumnName();
            final String javaProperty = column.getJavaProperty();
            final String jdbcTypeName = column.getJdbcTypeName();

            // 数据库自动生成的key，不需要手动指定value
            if (!StringUtils.equals(introspectedTable.getGeneratedKey().getColumn(), actualColumnName)) {
                columnNames.add(actualColumnName);
                valueFields.add(String.format("#{item.%s,jdbcType=%s}", javaProperty, jdbcTypeName));
            }
        }

        // 添加列名
        insertBatch.addElement(new TextElement(String.format("(%s)", String.join(",", columnNames))));
        insertBatch.addElement(new TextElement("values"));

        final XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("collection", "records"));
        foreach.addAttribute(new Attribute("item", "item"));
        foreach.addAttribute(new Attribute("separator", ","));

        foreach.addElement(new TextElement(String.format("(%s)", String.join(",", valueFields))));

        insertBatch.addElement(foreach);
        rootElement.addElement(insertBatch);
    }


    /**
     * 一个表中如果有一些字段是数据库自动生成的（有default设置），不需要手动指定的话，那么这些字段可以不在sql中出现
     * 在简单的insertSelective只有每次只插入一个record，可以通过record中的属性是否为null来推断该字段是否需要出现在sql中
     * 但insert batch有一堆record，所以多个参数，用来手动指定哪些字段需要skip，不需要出现在sql中
     * @param document
     * @param introspectedTable
     */
    private void sqlMapInsertBatchSelective(Document document, IntrospectedTable introspectedTable) {
        // insertBatch

        final XmlElement rootElement = document.getRootElement();
        final XmlElement insertBatch = new XmlElement("insert");
        context.getCommentGenerator().addComment(insertBatch);

        insertBatch.addAttribute(new Attribute("id", "insertBatchSelective"));
        insertBatch.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));


        final List<String> columnNames = new ArrayList<>(); // 组成insert into table (col1,col2) values ( ) 的col1,col2
        final List<String> valueFields = new ArrayList<>(); // 组成insert into table (col1,col2) values ( ) 的( )中间的部分
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            final String actualColumnName = column.getActualColumnName();
            final String javaProperty = column.getJavaProperty();
            final String jdbcTypeName = column.getJdbcTypeName();

            // 数据库自动生成的key，不需要手动指定value
            if (!StringUtils.equals(introspectedTable.getGeneratedKey().getColumn(), actualColumnName)) {
                columnNames.add(actualColumnName);
                valueFields.add(String.format("#{item.%s,jdbcType=%s}", javaProperty, jdbcTypeName));
            }
        }

        // 添加列名
        XmlElement columnsTrim = new XmlElement("trim");
        columnsTrim.addAttribute(new Attribute("suffixOverrides",","));
        columnsTrim.addAttribute(new Attribute("prefix","("));
        columnsTrim.addAttribute(new Attribute("suffix",")"));

        XmlElement valuePlaceholdersTrim = new XmlElement("trim");
        valuePlaceholdersTrim.addAttribute(new Attribute("suffixOverrides",","));
        valuePlaceholdersTrim.addAttribute(new Attribute("prefix","("));
        valuePlaceholdersTrim.addAttribute(new Attribute("suffix",")"));
        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            String valueField = valueFields.get(i);

            XmlElement colIfEl = new XmlElement("if");
            colIfEl.addAttribute(
                    new Attribute("test", String.format("excludeColNames==null or !excludeColNames.contains('%s')",columnName)));
            colIfEl.addElement(new TextElement(columnName+','));
            columnsTrim.addElement(colIfEl);

            XmlElement valuePlaceholderIfEl = new XmlElement("if");
            valuePlaceholderIfEl.addAttribute(
                    new Attribute("test", String.format("excludeColNames==null or !excludeColNames.contains('%s')",columnName)));
            valuePlaceholderIfEl.addElement(new TextElement(valueField+','));
            valuePlaceholdersTrim.addElement(valuePlaceholderIfEl);
        }

        insertBatch.addElement(columnsTrim);
        insertBatch.addElement(new TextElement("values"));

        final XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("collection", "records"));
        foreach.addAttribute(new Attribute("item", "item"));
        foreach.addAttribute(new Attribute("separator", ","));

        foreach.addElement(valuePlaceholdersTrim);

        insertBatch.addElement(foreach);
        rootElement.addElement(insertBatch);
    }

}
