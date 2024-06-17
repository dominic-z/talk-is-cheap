package codegen.generator.mbg.plugin.bussinesssql;

import codegen.config.CodeGeneratorConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.XmlFormatter;
import org.mybatis.generator.api.dom.DefaultXmlFormatter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.PublicDocType;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.io.FileOutputStream;
import java.io.StringBufferInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 生成一个新的sqlMapper，用于开发时写自定义的sql语句，将mgb生成的文件与自定义sql文件拆分
 *
 * @author dominiczhu
 * @version 1.0
 * @title GenerateAnotherSqlmapPlugin
 * @date 2022/1/14 10:35 上午
 */
@Slf4j
public class GenerateAnotherSqlMapForCustomPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {

        try {
            final Field documentField = GeneratedXmlFile.class.getDeclaredField("document");
            documentField.setAccessible(true);
            final Document document = (Document) documentField.get(sqlMap);
            final PublicDocType publicDocType = (PublicDocType) document.getDocType().get();

            final XmlElement rootElement = document.getRootElement();

            final XmlElement baseResultMap = getBaseResultMap(rootElement);
            final XmlElement baseColumn = getBaseColumnList(rootElement);

            XmlFormatter formatter = new DefaultXmlFormatter();
            final Document customizeDocument = new Document(publicDocType.getDtdName(), publicDocType.getDtdLocation());

            final XmlElement newRootElement = new XmlElement("mapper");
            newRootElement.addAttribute(new Attribute("namespace",
                    CodeGeneratorConfig.getInstance().getCustomizedDaoPackage() + "." +
                            introspectedTable.getTableConfiguration().getDomainObjectName() + "Dao"));
            newRootElement.addElement(baseResultMap);
            newRootElement.addElement(baseColumn);


            newRootElement.addElement(new TextElement("<!--"));
            newRootElement.addElement(new TextElement("if you want to create a customized sql, add it here"));
            newRootElement.addElement(new TextElement("For example"));
            newRootElement.addElement(new TextElement("<select id=\"select_by_ids\" resultMap=\"BaseResultMap\">"));
            newRootElement.addElement(new TextElement("select"));
            newRootElement.addElement(new TextElement("<include refid=\"Base_Column_List\"/>"));
            newRootElement.addElement(new TextElement("from "+introspectedTable.getFullyQualifiedTableNameAtRuntime()));
            newRootElement.addElement(new TextElement("where id in"));
            newRootElement.addElement(new TextElement(
                    "<foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\">#{id}</foreach>"));
            newRootElement.addElement(new TextElement("</select>"));

            newRootElement.addElement(new TextElement("-->"));

            customizeDocument.setRootElement(newRootElement);

            final String formattedContent = formatter.getFormattedContent(customizeDocument);

            final Path customizedXmlPath = Paths.get(CodeGeneratorConfig.SQL_MAPPER_XML_ROOT_PATH, "customized",
                    introspectedTable.getTableConfiguration().getMapperName() + ".xml");
            if (!Files.exists(customizedXmlPath.getParent())) {
                Files.createDirectories(customizedXmlPath.getParent());
            }

            try (final FileOutputStream fileOutputStream = new FileOutputStream(customizedXmlPath.toFile())) {
                final byte[] bytes = formattedContent.getBytes(StandardCharsets.UTF_8);

                final FileChannel outputStreamChannel = fileOutputStream.getChannel();

                final int capacity = 1024;
                ByteBuffer buffer = ByteBuffer.allocate(capacity);
                for (int i = 0; i < bytes.length; i += capacity) {
                    buffer.put(bytes, i, Math.min(capacity, bytes.length - i));
                    buffer.flip();
                    outputStreamChannel.write(buffer);
                    buffer.clear();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException("error when parse GeneratedXmlFile by reflect");
        }

        return super.sqlMapGenerated(sqlMap, introspectedTable);
    }

    private XmlElement getBaseResultMap(XmlElement rootElement) {
        for (VisitableElement visitableElement : rootElement.getElements()) {
            if (visitableElement instanceof XmlElement) {
                XmlElement subXmlElement = (XmlElement) visitableElement;
                if (subXmlElement.getName().equals("resultMap")) {
                    boolean isBaseResultMap = false;
                    for (Attribute attribute : subXmlElement.getAttributes()) {
                        if (attribute.getName().equals("id") && attribute.getValue().equals("BaseResultMap")) {
                            isBaseResultMap = true;
                            break;
                        }
                    }
                    if (isBaseResultMap) {
                        return subXmlElement;
                    }

                }
            }
        }
        return null;
    }

    private XmlElement getBaseColumnList(XmlElement rootElement) {
        for (VisitableElement visitableElement : rootElement.getElements()) {
            if (visitableElement instanceof XmlElement) {
                XmlElement subXmlElement = (XmlElement) visitableElement;
                if (subXmlElement.getName().equals("sql")) {
                    boolean isBaseColumnList = false;
                    for (Attribute attribute : subXmlElement.getAttributes()) {
                        if (attribute.getName().equals("id") && attribute.getValue().equals("Base_Column_List")) {
                            isBaseColumnList = true;
                            break;
                        }
                    }
                    if (isBaseColumnList) {
                        return subXmlElement;
                    }

                }
            }
        }
        return null;
    }

}
