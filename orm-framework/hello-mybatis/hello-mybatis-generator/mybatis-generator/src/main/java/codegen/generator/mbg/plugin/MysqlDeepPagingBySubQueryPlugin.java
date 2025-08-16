package codegen.generator.mbg.plugin;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

/**
 * 可以实现深度分页的插件，必须以id为主键，并且需要用到覆盖索引做子查询
 */
public class MysqlDeepPagingBySubQueryPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {

        XmlElement deepPagingSelect = new XmlElement("select");
        context.getCommentGenerator().addComment(deepPagingSelect);

        deepPagingSelect.addAttribute(new Attribute("id", "selectByExampleDeepPagingByIdSubQuery"));
        deepPagingSelect.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
        deepPagingSelect.addAttribute(new Attribute("resultMap", "BaseResultMap"));

        deepPagingSelect.addElement(new TextElement("select"));
        deepPagingSelect.addElement(new TextElement("<include refid=\"Base_Column_List\" />"));
        deepPagingSelect.addElement(new TextElement(String.format("from %s", introspectedTable.getFullyQualifiedTableNameAtRuntime())));

        XmlElement where = new XmlElement("where");
        where.addElement(new TextElement("id in ("));

//       这个子查询必须需要嵌套，
//SELECT * from role where id in (select sub.id from (SELECT id from role where name in ('管理员','运维') limit 2) as sub);
//        否则如果写成 SELECT * from role where id in (SELECT id from role where name in ('管理员','运维') limit 2)，会报错不支持这类sql
//        This version of MySQL doesn't yet support 'LIMIT & IN/ALL/ANY/SOME subquery'
        String subSelectQuery = """
                    select sub.id from
                    (
                    select id
                    from %s
                    <if test="_parameter != null">
                      <include refid="Example_Where_Clause" />
                    </if>
                    <if test="orderByClause != null">
                      order by ${orderByClause}
                    </if>
                    <if test="limit != null">
                      <if test="offset != null">
                        limit ${offset}, ${limit}
                      </if>
                      <if test="offset == null">
                        limit ${limit}
                      </if>
                    </if>
                    ) as sub
               """;
//        String subSelectQuery ="";
        where.addElement(new TextElement(String.format(subSelectQuery, introspectedTable.getFullyQualifiedTableNameAtRuntime())));
        where.addElement(new TextElement(")"));
        deepPagingSelect.addElement(where);

        document.getRootElement().addElement(deepPagingSelect);

        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }


    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {

        Method selectByExampleDeepPagingByIdSubQuery = new Method("selectByExampleDeepPagingByIdSubQuery");
        selectByExampleDeepPagingByIdSubQuery.addParameter(new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()),"example"));
        selectByExampleDeepPagingByIdSubQuery.setReturnType(new FullyQualifiedJavaType(String.format("List<%s>",introspectedTable.getBaseRecordType())));
        selectByExampleDeepPagingByIdSubQuery.setAbstract(true);
        interfaze.addMethod(selectByExampleDeepPagingByIdSubQuery);
        return super.clientGenerated(interfaze, introspectedTable);
    }
}
