package com.liu.wanxinp2p.repayment.config;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.liu.wanxinp2p.repayment.job.RepaymentJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticJobConfig {

    @Autowired
    private RepaymentJob repaymentJob;

    @Autowired
    ZookeeperRegistryCenter registryCenter;

    @Value("${job.shardingCount}")
    private int shardingCount;

    @Value("${job.cron}")
    private String cron;

    /**
     * 配置任务详细信息
     * @param jobClass 任务执行类型
     * @param cron 执行策略
     * @param shardingCount 分片数量
     * @return
     */
    private LiteJobConfiguration createJobConfiguration(Class<? extends SimpleJob> jobClass,
                                                        String cron,
                                                        int shardingCount) {
        //创建JobCoreConfigurationBuilder
        JobCoreConfiguration.Builder jobCoreConfigurationBuilder = JobCoreConfiguration.newBuilder(jobClass.getName(),
                cron,shardingCount);
        JobCoreConfiguration jobCoreConfiguration = jobCoreConfigurationBuilder.build();
        //创建SimpleJobConfiguration
        SimpleJobConfiguration simpleJobConfiguration = new SimpleJobConfiguration(jobCoreConfiguration,jobClass.getCanonicalName());
        //创建LiteJobConfiguration
        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(simpleJobConfiguration).overwrite(true).build();
        return liteJobConfiguration;
    }

    @Bean(initMethod = "init")
    public SpringJobScheduler initSimpleElasticJob() {
        //创建SpringJobScheduler
        SpringJobScheduler scheduler = new SpringJobScheduler(repaymentJob,registryCenter,createJobConfiguration(repaymentJob.getClass(),cron,shardingCount));
        return scheduler;
    }
}
