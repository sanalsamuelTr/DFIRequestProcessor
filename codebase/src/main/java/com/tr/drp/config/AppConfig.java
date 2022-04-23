package com.tr.drp.config;

import com.tr.drp.jobs.JobIdFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AppConfig {
    @Bean
    public JobIdFormat jobIdFormat(@Value("${app.jobid-pattern}") String jobIdPattern) {
        return new JobIdFormat(jobIdPattern);
    }
}
