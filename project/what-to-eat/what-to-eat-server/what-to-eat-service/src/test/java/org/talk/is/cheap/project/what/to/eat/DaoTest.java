package org.talk.is.cheap.project.what.to.eat;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.talk.is.cheap.project.what.to.eat.dao.mbg.BusinessMapper;
import org.talk.is.cheap.project.what.to.eat.domain.pojo.Business;
import org.talk.is.cheap.project.what.to.eat.domain.query.example.BusinessExample;

@SpringBootTest(classes = Application.class)
@Slf4j
public class DaoTest {

    @Autowired
    private BusinessMapper businessMapper;

    @Test
    public void testBusinessMapper() {
        businessMapper.insertSelective(new Business().withName("小猪便利店").withAvatarPath("null"));
        val businessExample = new BusinessExample();
        businessExample.createCriteria().andIdEqualTo(1L);
        log.info("{}", businessMapper.selectByExample(businessExample));
    }
}
