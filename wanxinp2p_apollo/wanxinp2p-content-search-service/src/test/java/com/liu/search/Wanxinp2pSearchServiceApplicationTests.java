package com.liu.search;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class Wanxinp2pSearchServiceApplicationTests {

    @Value("${wanxinp2p.es.host}")
    private String host;

    @Test
    public void contextLoads() {
        System.out.println(host);
    }

}
