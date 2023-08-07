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
import java.util.Date;

/**
* 定制化的service层，用于弥补mbg生成的mapper过于灵活导致可能出现的业务漏洞，例如越过deleted字段查询、更新updateTime等
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
        return ${mapperLowerCamelName}.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public int createBatch(Collection<${domainUpperCamelName}> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return ${mapperLowerCamelName}.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByExample(${exampleUpperCamelName} example) {
        if (example == null) {
            return 0;
        }
        return ${mapperLowerCamelName}.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByExampleSelective(${domainUpperCamelName} record, ${exampleUpperCamelName} example) {
        if (record == null || example == null) {
            return 0;
        }
        record.setUpdateTime(new Date());
        return ${mapperLowerCamelName}.updateByExampleSelective(record, example);
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

    // 基于${daoLowerCamelName}

}
