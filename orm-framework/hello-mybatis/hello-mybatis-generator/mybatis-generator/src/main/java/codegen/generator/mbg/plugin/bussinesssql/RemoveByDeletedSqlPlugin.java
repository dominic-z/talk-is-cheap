package codegen.generator.mbg.plugin.bussinesssql;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
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
 * 新增通过deleted字段来标识删除的update sql  在dmp工作时用的
 *
 * @author dominiczhu
 * @version 1.0
 * @title RemoveByDeletedSqlPlugin
 * @date 2022/1/14 9:38 上午
 */
public class RemoveByDeletedSqlPlugin extends PluginAdapter {

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
        sqlMapRemoveByPrimaryKey(document, introspectedTable);

        sqlMapRemoveByPrimaryKeys(document, introspectedTable);

        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {

        clientRemoveByPrimaryKey(interfaze, introspectedTable);


        clientRemoveByPrimaryKeys(interfaze, introspectedTable);

        return true;
    }


    private void clientRemoveByPrimaryKey(Interface interfaze, IntrospectedTable introspectedTable) {
        // removeByPrimaryKey
        Method removeByPrimaryKeyMethod = new Method("removeByPrimaryKey");
        removeByPrimaryKeyMethod.setReturnType(PrimitiveTypeWrapper.getIntInstance());
        removeByPrimaryKeyMethod.setAbstract(true);
        context.getCommentGenerator().addGeneralMethodComment(removeByPrimaryKeyMethod, introspectedTable);

        Parameter removeByPrimaryKeyParameter = new Parameter(new FullyQualifiedJavaType("long"), "id");
        removeByPrimaryKeyMethod.addParameter(removeByPrimaryKeyParameter);
        interfaze.addMethod(removeByPrimaryKeyMethod);
    }

    private void clientRemoveByPrimaryKeys(Interface interfaze, IntrospectedTable introspectedTable) {
        // removeByPrimaryKeys
        Method removeByPrimaryKeyMethods = new Method("removeByPrimaryKeys");
        removeByPrimaryKeyMethods.setReturnType(PrimitiveTypeWrapper.getIntInstance());
        removeByPrimaryKeyMethods.setAbstract(true);
        context.getCommentGenerator().addGeneralMethodComment(removeByPrimaryKeyMethods, introspectedTable);

        Parameter removeByPrimaryKeysMethod = new Parameter(new FullyQualifiedJavaType("Collection<Long>"), "ids");
        removeByPrimaryKeyMethods.addParameter(removeByPrimaryKeysMethod);
        interfaze.addMethod(removeByPrimaryKeyMethods);
    }


    private void sqlMapRemoveByPrimaryKeys(Document document, IntrospectedTable introspectedTable) {
        // removeByPrimaryKeys
        final XmlElement rootElement = document.getRootElement();
        XmlElement removeByPrimaryKeys = new XmlElement("update");
        context.getCommentGenerator().addComment(removeByPrimaryKeys);

        removeByPrimaryKeys.addAttribute(new Attribute("id", "removeByPrimaryKeys"));
        removeByPrimaryKeys.addAttribute(new Attribute("parameterType", "java.util.Collection"));

        removeByPrimaryKeys.addElement(new TextElement("update " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        removeByPrimaryKeys.addElement(new TextElement("set deleted = null, revision = revision + 1"));
        removeByPrimaryKeys.addElement(new TextElement("where id in"));

        XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("collection", "collection"));
        foreach.addAttribute(new Attribute("item", "id"));
        foreach.addAttribute(new Attribute("open", "("));
        foreach.addAttribute(new Attribute("separator", ","));
        foreach.addAttribute(new Attribute("close", ")"));
        foreach.addElement(new TextElement("#{id}"));
        removeByPrimaryKeys.addElement(foreach);

        rootElement.addElement(removeByPrimaryKeys);
    }

    private void sqlMapRemoveByPrimaryKey(Document document, IntrospectedTable introspectedTable) {
        // removeByPrimaryKey
        final XmlElement rootElement = document.getRootElement();

        XmlElement removeByPrimaryKey = new XmlElement("update");
        context.getCommentGenerator().addComment(removeByPrimaryKey);

        removeByPrimaryKey.addAttribute(new Attribute("id", "removeByPrimaryKey"));
        removeByPrimaryKey.addAttribute(new Attribute("parameterType", "long"));

        removeByPrimaryKey.addElement(new TextElement("update " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        removeByPrimaryKey.addElement(new TextElement("set deleted = null, revision = revision + 1"));
        removeByPrimaryKey.addElement(new TextElement("where id = #{id}"));

        rootElement.addElement(removeByPrimaryKey);
    }
}
