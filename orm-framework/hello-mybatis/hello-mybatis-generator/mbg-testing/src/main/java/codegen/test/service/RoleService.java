package codegen.test.service;

import codegen.test.dao.mbg.RoleMapper;
import codegen.test.dao.customized.RoleDao;
import codegen.test.domain.pojo.Role;
import codegen.test.domain.query.example.RoleExample;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.lang.IllegalArgumentException;

/**
* 定制化的service层，用于弥补mbg生成的mapper过于灵活导致可能出现的业务漏洞，例如越过deleted字段查询、更新updateTime等
* @author dominiczhu
* @date 2025/08/16
*/
@Service
public class RoleService{

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private RoleMapper roleMapper;

    // 基于RoleMapper

    @Transactional(rollbackFor = Exception.class )
    public int create(Role record) {
        if (record == null) {
            return 0;
        }
        return roleMapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public int createBatch(Collection<Role> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return roleMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByExample(RoleExample example) {
        if (example == null) {
            return 0;
        }
        return roleMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByExampleSelective(Role record, RoleExample example) {
        if (record == null || example == null) {
            return 0;
        }
        // record.setUpdateTime(new Date()); // 通过数据库触发器实现
        return roleMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(RoleExample example) {
        if (example == null) {
            return 0L;
        }

        return roleMapper.countByExample(example);
    }

    public List<Role> selectByExample(RoleExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return roleMapper.selectByExample(example);
    }

    // 深度分页的service接口
    public List<Role> selectByExampleDeepPaging(RoleExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        if (example.getLimit() == null || example.getOffset() == null){
            throw new IllegalArgumentException("limit or offset can't be null");
        }
        return roleMapper.selectByExampleDeepPagingByIdSubQuery(example);
    }

    // 基于roleDao

}
