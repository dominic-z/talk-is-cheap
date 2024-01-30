package codegen.generator.mbg.plugin;

import codegen.CodeGeneratorManager;
import codegen.config.CodeGeneratorConfig;
import codegen.model.ColumnInfo;
import codegen.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.List;

@Slf4j
public class AddEnumToModelPlugin extends PluginAdapter {

    enum C {
        a_1
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * 给model类添加一些用来标识列名的静态常量，这些列名可以用来作为orderByClause的参数
     *
     * @param topLevelClass     the generated base record class
     * @param introspectedTable The class containing information about the table as
     *                          introspected from the database
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        val tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();

        val config = CodeGeneratorConfig.getInstance();
        CodeGeneratorConfig.TableConfig tableConfig = config.getTableConfigMap().get(tableName);
//        for (CodeGeneratorConfig.TableConfig c : config.getTables()) {
//            if (StringUtils.equals(c.getName(), tableName)) {
//                tableConfig = c;
//                break;
//            }
//        }
        val currentTableInfo = CodeGeneratorManager.getCurrentTableInfo();
        if (tableConfig == null || !StringUtils.equals(currentTableInfo.getTableName(), tableName)) {
            log.error("{} 查询表配置或者读取元数据为空", tableName);
            return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
        }

        if (tableConfig.getColumns() == null || tableConfig.getColumns().isEmpty()) {
            log.debug("{} 无需生成枚举类，列配置为空", tableName);
            return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
        }

        for (CodeGeneratorConfig.TableConfig.ColumnConfig columnConfig : tableConfig.getColumns()) {
            val columnName = columnConfig.getName();
            val columnInfo = currentTableInfo.getColumnInfoMap().get(columnName);
            if (columnInfo == null) {
                log.error("{} 列 {} 元数据信息为空", tableName, columnName);
                continue;
            }

            if (columnInfo.getPropertyType() == null) {
                log.error("{} 列 {} 不识别的类型，生成枚举类型错误", tableName, columnName);
                continue;
            }

//            new InnerEnumRenderer()

            val innerEnum = new InnerEnum(StringUtil.underScoreToUpperCamel(columnName) + "Enum");
            innerEnum.setVisibility(JavaVisibility.PUBLIC);
            innerEnum.setStatic(true);
            addFieldToEnum(columnInfo, innerEnum);


            boolean generateEnumSuccess = true;
            for (CodeGeneratorConfig.TableConfig.ColumnConfig.EnumeratedConstant enumeratedConstant :
                    columnConfig.getEnumeratedConstants()) {
                if (!isLegalEnumLiteral(enumeratedConstant.getLiteral())) {
                    log.error("列{}生成枚举类错误，{} 不是合法的枚举字面量", columnName, enumeratedConstant.getLiteral());
                    generateEnumSuccess = false;
                    break;
                }

                String valueInJavaCode = getValueInJavaCode(columnInfo, enumeratedConstant.getValue());

                if (valueInJavaCode == null) {
                    log.error("列{}生成枚举类错误，类型{} 无法转换为value对应的Java代码中表征的值，仅支持数字、String", columnName,
                            columnInfo.getPropertyType());
                    generateEnumSuccess = false;
                    break;
                }

//                构造字面量，格式为A(value,"描述")
                innerEnum.addEnumConstant(String.format("%s(%s,\"%s\")", enumeratedConstant.getLiteral(),
                        valueInJavaCode, enumeratedConstant.getDescription()));
            }

            //                创建构造函数
            final Method constructor = getConstructor(columnInfo, innerEnum);
            innerEnum.addMethod(constructor);
//                创建getter
            val valueGetter = makeGetter("getValue", columnInfo.getPropertyType().getTypeName(), "value");
            val descriptionGetter = makeGetter("getDescription", columnInfo.getPropertyType().getTypeName(),
                    "description");
            innerEnum.addMethod(valueGetter);
            innerEnum.addMethod(descriptionGetter);


            if (generateEnumSuccess) {
                topLevelClass.addInnerEnum(innerEnum);
            }
        }
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 为enum里面增加value与description变量,为枚举类创建value字段用于存储枚举值，description字段用于存储描述
     *
     * @param columnInfo
     * @param innerEnum
     */
    private static void addFieldToEnum(ColumnInfo columnInfo, InnerEnum innerEnum) {
        val enumValue = new Field("value",
                new FullyQualifiedJavaType(columnInfo.getPropertyType().getTypeName()));
        enumValue.setVisibility(JavaVisibility.PRIVATE);
        enumValue.setFinal(true);
        innerEnum.addField(enumValue);
        val description = new Field("description", new FullyQualifiedJavaType("String"));
        description.setVisibility(JavaVisibility.PRIVATE);
        description.setFinal(true);
        innerEnum.addField(description);
    }

    /**
     * 创建构造函数，生成如下的方法
     * private  enumName(SomeType value, String description){
     * this.value = value;
     * this.description = description;
     * }
     */
    private static Method getConstructor(ColumnInfo columnInfo, InnerEnum innerEnum) {
        val constructor = new Method(innerEnum.getType().getShortNameWithoutTypeArguments());
        constructor.setVisibility(JavaVisibility.PRIVATE);
        val valueParam = new Parameter(new FullyQualifiedJavaType(columnInfo.getPropertyType().getTypeName()),
                "value");
        val desParam = new Parameter(new FullyQualifiedJavaType("String"), "description");
        constructor.addParameter(valueParam);
        constructor.addParameter(desParam);
        constructor.addBodyLine("this.value = value;");
        constructor.addBodyLine("this.description = description;");
        return constructor;
    }

    /**
     * 将枚举值的value转换为java代码，例如，如果变量是一个String，那么生成的的枚举变量就应该是A("someValue","一些描述")
     *
     * @param columnInfo
     * @param enumeratedConstantValue
     * @return
     */
    private static String getValueInJavaCode(ColumnInfo columnInfo,
                                             String enumeratedConstantValue) {
        switch (columnInfo.getPropertyType()) {
            case LongType:
            case FloatType:
            case DoubleType:
            case IntegerType:
                return enumeratedConstantValue;
            case StringType:
                return String.format("\"%s\"", enumeratedConstantValue);
            default:
        }
        return null;
    }

    private static Method makeGetter(String methodName, String returnType, String getterTarget) {
        val getter = new Method(methodName);
        getter.setVisibility(JavaVisibility.PUBLIC);
        getter.setReturnType(new FullyQualifiedJavaType(returnType));
        getter.addBodyLine("return this." + getterTarget);
        return getter;
    }

    /**
     * 是否是合法的枚举字面量，首字母不能是数字下划线，只能包含数字字母下划线
     *
     * @param enumLiteral
     * @return
     */
    private boolean isLegalEnumLiteral(String enumLiteral) {
        if (StringUtils.isBlank(enumLiteral)) {
            return false;
        }
        for (int i = 0; i < enumLiteral.toCharArray().length; i++) {
            char c = enumLiteral.charAt(i);

            if (i == 0 && ((c >= '0' && c <= '9') || c == '_')) {
                return false;
            }
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_')) {
                return false;
            }
        }
        return true;
    }
}
