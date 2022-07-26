package com.liu.wanxinp2p.repayment.config;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZKRegistryCenterConfig {

    //zookeeper地址
    @Value("${zookeeper.connString}")
    private String ZOOKEEPER_CONNECTION_STRING;

    //任务命名空间
    @Value("${job.namespace}")
    private String JOB_NAMESPACE;

    //创建注册中心

    /**
     * initMethod=init 标志着这个方法要在实例变量初始化之后来执行
     * @return
     */
    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter setUpRegistryCenter() {
        //zk的配置
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(ZOOKEEPER_CONNECTION_STRING,JOB_NAMESPACE);
        //创建注册中心
        ZookeeperRegistryCenter zookeeperRegistryCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        return zookeeperRegistryCenter;
    }
}
