package com.tr.drp.config;

import com.tr.drp.jobs.JobIdFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class AppConfig {

    @Bean
    public JobIdFormat jobIdFormat(@Value("${app.jobid-pattern}") String jobIdPattern) {
        return new JobIdFormat(jobIdPattern);
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler
                = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setThreadNamePrefix(
                "SchedulerTask");
        return threadPoolTaskScheduler;
    }
}
