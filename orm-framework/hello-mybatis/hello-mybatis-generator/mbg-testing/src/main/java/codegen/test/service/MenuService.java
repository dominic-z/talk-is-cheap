package codegen.test.service;

import codegen.test.dao.mbg.MenuMapper;
import codegen.test.dao.customized.MenuDao;
import codegen.test.domain.pojo.Menu;
import codegen.test.domain.query.example.MenuExample;

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
* @author dominiczhu
* @date 2024/01/18
*/
@Service
public class MenuService{

    @Autowired
    private MenuDao menuDao;

    @Autowired
    private MenuMapper menuMapper;

    // 基于MenuMapper

    @Transactional(rollbackFor = Exception.class)
    public int create(Menu record) {
        if (record == null) {
            return 0;
        }
        return menuMapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public int createBatch(Collection<Menu> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return menuMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByExample(MenuExample example) {
        if (example == null) {
            return 0;
        }
        return menuMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByExampleSelective(Menu record, MenuExample example) {
        if (record == null || example == null) {
            return 0;
        }
//        record.setUpdateTime(new Date());
        return menuMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(MenuExample example) {
        if (example == null) {
            return 0L;
        }

        return menuMapper.countByExample(example);
    }

    public List<Menu> selectByExample(MenuExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return menuMapper.selectByExample(example);
    }

    // 基于menuDao

}
