package com.tr.drp;

import com.tr.drp.common.utils.CollectionsUtils;
import com.tr.drp.config.DomainProperties;
import com.tr.drp.config.properties.AppProperties;
import com.tr.drp.jobs.DBJobRunnerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

@SpringBootApplication
public class Main implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static ConfigurableApplicationContext applicationContext;

    @Autowired
    private AbstractEnvironment env;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private DBJobRunnerFactory dbJobRunnerFactory;

    @Autowired
    private TaskScheduler taskScheduler;


    public static void main(String... args) {
        log.info("STARTING APPLICATION");
        applicationContext = SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("RUN");

        addDbSchedulers(taskScheduler);
    }

    private void addDbSchedulers(TaskScheduler taskScheduler) {
        for (DomainProperties domainProperties: CollectionsUtils.safe(appProperties.getDomains())
                .stream().filter(d -> StringUtils.isNotBlank(d.getDbCron())).collect(Collectors.toList())) {
            String cron = domainProperties.getDbCron();
            taskScheduler.schedule(dbJobRunnerFactory.createRunner(domainProperties.getName()), new CronTrigger(cron));
        }
    }

    @PostConstruct
    public void init() {
        env.setIgnoreUnresolvableNestedPlaceholders(true);
    }

    public static ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
