package codegen.generator.mbg.plugin.bussinesssql;

/**
 * @author dominiczhu
 * @version 1.0
 * @title MysqlLimitPlugin
 * @date 2021/9/24 下午5:46
 * https://blog.csdn.net/xiao__gui/article/details/51333693
 */

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

/**
 * 使用remove来标识删除，再各个update select语句之中增加deleted字段 在dmp工作时用的
 */
public class SelectUpdateWithDeletedDefaultPlugin extends PluginAdapter {

    private static final String AND_DELETED_EQ_0 = "and deleted = 0";

    @Override
    public boolean validate(List<String> list) {
        return true;
    }


    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
                                                                     IntrospectedTable introspectedTable) {

        replaceIfParameterNullByDeletedWhere(element);

        return true;
    }


    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        element.addElement(new TextElement(AND_DELETED_EQ_0));
        return true;
    }

    @Override
    public boolean sqlMapCountByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {

        replaceIfParameterNullByDeletedWhere(element);
        return true;
    }

//    @Override
//    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element,
//                                                                     IntrospectedTable introspectedTable) {
//        element.addElement(new TextElement(AND_DELETED_EQ_0));
//        return true;
//    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element,
                                                                        IntrospectedTable introspectedTable) {
        element.addElement(new TextElement(AND_DELETED_EQ_0));
        return true;
    }

    @Override
    public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
//        将Example_Where_Clause的最外层where删掉；
        int whereIndex = -1;
        for (int i = 0; i < element.getElements().size(); i++) {

            if (element.getElements().get(i) instanceof XmlElement) {
                final XmlElement xmlElement = (XmlElement) element.getElements().get(i);
                if ("where".equals(xmlElement.getName())) {
                    whereIndex = i;
                }
            }
        }

        if (whereIndex != -1) {
            XmlElement where = (XmlElement) element.getElements().get(whereIndex);
            XmlElement foreach = (XmlElement) where.getElements().get(0);
            element.getElements().set(whereIndex, foreach);
        }

        return super.sqlMapExampleWhereClauseElementGenerated(element, introspectedTable);
    }

    /**
     * 将下面这个标签
     * <if test="_parameter != null">
     * <include refid="Example_Where_Clause" /> 或者 <include refid="Update_Example_Where_Clause" />
     * </if>
     * 变为
     * <where>
     * deleted = 0
     * <if test="_parameter != null">
     * and (
     * <include refid="Example_Where_Clause" /> 或者 <include refid="Update_Example_Where_Clause" />
     * )
     * </if>
     * </where>
     *
     * @param element
     */
    private void replaceIfParameterNullByDeletedWhere(XmlElement element) {
        final List<VisitableElement> subElements = element.getElements();

        final int ifParameterNullIndex = getIfParameterNullIndex(element.getElements());
        if (ifParameterNullIndex < 0) {
            return;
        }
        final XmlElement ifParameterNull = (XmlElement) subElements.get(ifParameterNullIndex);

        final XmlElement include = (XmlElement) ifParameterNull.getElements().get(0);
        final XmlElement deletedAndParameterWhere = getDeletedAndParameterWhere(include);
        element.getElements().set(ifParameterNullIndex, deletedAndParameterWhere);
    }

    /**
     * selectByExample里搜索<if test="_parameter != null">这个标签
     *
     * @param subElements
     * @return
     */
    private int getIfParameterNullIndex(List<VisitableElement> subElements) {
        int ifParameterNullIndex = -1;
        for (int i = 0; i < subElements.size(); i++) {
            VisitableElement subElement = subElements.get(i);
            if (subElement instanceof XmlElement) {
                XmlElement xmlSubElement = (XmlElement) subElement;
                if (!xmlSubElement.getName().equals("if")) {
                    continue;
                }

                final List<Attribute> attributes = xmlSubElement.getAttributes();
                if (attributes.isEmpty()) {
                    continue;
                }

                if (attributes.get(0).getName().equals("test") &&
                        attributes.get(0).getValue().equals("_parameter != null")) {
                    ifParameterNullIndex = i;
                    break;
                }
            }
        }
        return ifParameterNullIndex;
    }

    /**
     * 生成如下标签
     * <where>
     * deleted = 0
     * <if test="_parameter != null">
     * and (
     * include
     * )
     * </if>
     * </where>
     *
     * @param include 需要插入在and (  )的括号中间的一个标签
     */
    private XmlElement getDeletedAndParameterWhere(XmlElement include) {
        XmlElement newWhere = new XmlElement("where");
        newWhere.addElement(new TextElement("deleted = 0"));

        XmlElement newIfParameterNull = new XmlElement("if");

        newIfParameterNull.addAttribute(new Attribute("test", "_parameter != null"));
        newIfParameterNull.addElement(new TextElement("and ("));

        newIfParameterNull.addElement(include);
        newIfParameterNull.addElement(new TextElement(")"));

        newWhere.addElement(newIfParameterNull);

        return newWhere;
    }
}
