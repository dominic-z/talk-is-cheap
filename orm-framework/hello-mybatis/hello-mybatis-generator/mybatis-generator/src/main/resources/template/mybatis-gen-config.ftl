<!DOCTYPE generatorConfiguration PUBLIC
        "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
<generatorConfiguration>
    <context id="dsql" targetRuntime="MyBatis3">
        <plugin type="org.mybatis.generator.plugins.FluentBuilderMethodsPlugin"/>
        <plugin type="org.mybatis.generator.plugins.ToStringPlugin"/>
        <plugin type="org.mybatis.generator.plugins.SerializablePlugin"/>
        <plugin type="org.mybatis.generator.plugins.EqualsHashCodePlugin"/>
        <plugin type="org.mybatis.generator.plugins.RowBoundsPlugin"/>
        <plugin type="codegen.generator.mbg.plugin.MysqlLimitPlugin"/>
        <plugin type="codegen.generator.mbg.plugin.CommentPlugin"/>
        <plugin type="codegen.generator.mbg.plugin.AddColumnNameToModelPlugin"/>
        <plugin type="codegen.generator.mbg.plugin.bussinesssql.CloseSomeFuncPlugin"/>
        <plugin type="codegen.generator.mbg.plugin.bussinesssql.GenerateAnotherSqlMapForCustomPlugin"/>
        <plugin type="codegen.generator.mbg.plugin.bussinesssql.InsertBatchSqlPlugin"/>
        <plugin type="codegen.generator.mbg.plugin.bussinesssql.MbgRunningDataCollectorPlugin"/>
        <!-- MySQL分页插件 -->

        <commentGenerator>
            <property name="addRemarkComments" value="true"/>
        </commentGenerator>

        <jdbcConnection driverClass="${JDBC_DRIVER}"
                        connectionURL="${JDBC_URL}" userId="${JDBC_USERNAME}" password="${JDBC_PASSWORD}"/>

        <javaModelGenerator targetPackage="${modelPackage}"
                            targetProject="${JAVA_PATH}">
            <property name="exampleTargetPackage"
                      value="${mgbExamplePackage}"/>
        </javaModelGenerator>

        <sqlMapGenerator targetPackage="mbg"
                         targetProject="${SQL_MAPPER_XML_ROOT_PATH}"/>

        <javaClientGenerator type="XMLMAPPER"
                             targetPackage="${mbgMapperPackage}"
                             targetProject="${JAVA_PATH}"/>

        <table tableName="${tableName}" domainObjectName="${domainUpperCamelName}"
               mapperName="${mapperUpperCamelName}" modelType="flat">
               <!-- insert语句需要回填id，目前我的主键都是id，没出现过复合主键 -->
               <generatedKey column="id" sqlStatement="MySql" identity="true"/>
        </table>

    </context>
</generatorConfiguration>