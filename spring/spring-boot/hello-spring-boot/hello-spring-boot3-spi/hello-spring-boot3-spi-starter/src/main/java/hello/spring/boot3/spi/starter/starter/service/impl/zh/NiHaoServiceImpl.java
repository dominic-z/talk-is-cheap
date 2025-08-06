package hello.spring.boot3.spi.starter.starter.service.impl.zh;

import hello.spring.boot3.spi.starter.starter.service.NiHaoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NiHaoServiceImpl implements NiHaoService {

    @Override
    public String nihao() {
        log.info("nihao from starter");
        return "nihao from starter";
    }
}
