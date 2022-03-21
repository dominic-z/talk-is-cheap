package ${servicePackage};

import ${mbgMapperPackage}.${mapperUpperCamelName};
import ${customizedDaoPackage}.${daoUpperCamelName};
import ${modelPackage}.${domainUpperCamelName};
import ${mgbExamplePackage}.${exampleUpperCamelName};
<#if primaryKeyFullyQualifiedName??>import ${primaryKeyFullyQualifiedName};</#if>
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
* @author ${author}
* @date ${date}
*/
@Service
public class ${serviceUpperCamelName}{

    @Autowired
    private ${daoUpperCamelName} ${daoLowerCamelName};

    @Autowired
    private ${mapperUpperCamelName} ${mapperLowerCamelName};

    // 基于${mapperUpperCamelName}

    @Transactional(rollbackFor = Exception.class)
    public int create(${domainUpperCamelName} record) {
        if (record == null) {
            return 0;
        }
        return ${mapperLowerCamelName}.insert(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public int createBatch(Collection<${domainUpperCamelName}> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return ${mapperLowerCamelName}.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryKey(${primaryKeyShortName} key) {
        if (key == null) {
            return 0;
        }
        return ${mapperLowerCamelName}.deleteByPrimaryKey(key);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByPrimaryKey(${domainUpperCamelName} record) {
        if (record == null) {
            return 0;
        }
        return ${mapperLowerCamelName}.updateByPrimaryKey(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByExampleSelective(${domainUpperCamelName} record, ${exampleUpperCamelName} example) {
        if (record == null || example == null) {
            return 0;
        }
        return ${mapperLowerCamelName}.updateByExampleSelective(record, example);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByExample(${domainUpperCamelName} record, ${exampleUpperCamelName} example) {
        if (record == null || example == null) {
            return 0;
        }
        return ${mapperLowerCamelName}.updateByExample(record, example);
    }


    public long countByExample(${exampleUpperCamelName} example) {
        if (example == null) {
            return 0L;
        }

        return ${mapperLowerCamelName}.countByExample(example);
    }

    public List<${domainUpperCamelName}> selectByExample(${exampleUpperCamelName} example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return ${mapperLowerCamelName}.selectByExample(example);
    }

    public ${domainUpperCamelName} selectByPrimaryKey(${primaryKeyShortName} key) {
        if (key == null) {
            return null;
        }
        return ${mapperLowerCamelName}.selectByPrimaryKey(key);
    }

    // 基于${daoLowerCamelName}

}
