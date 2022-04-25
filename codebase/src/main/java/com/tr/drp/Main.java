package com.tr.drp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.AbstractEnvironment;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class Main implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static ConfigurableApplicationContext applicationContext;

    @Autowired
    private AbstractEnvironment env;

    @Autowired
    private Job inboundJob;


    public static void main(String... args) {
        log.info("STARTING APPLICATION");
        applicationContext = SpringApplication.run(Main.class, args);
//        log.info("APPLICATION FINISHED");
//        applicationContext.close();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("RUN");
    }

    @PostConstruct
    public void init() {
        env.setIgnoreUnresolvableNestedPlaceholders(true);
    }

    public static ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
