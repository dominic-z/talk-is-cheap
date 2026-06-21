package org.talk.is.cheap.mq.hello.mq;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talk.is.cheap.mq.hello.rocketmq.quickstart.ProducerExample;

public class Demo {

    private static final Logger logger = LoggerFactory.getLogger(Demo.class);
    

    @Test
    public void testLog(){
        logger.info("hahah {}","asdf");

    }
}
