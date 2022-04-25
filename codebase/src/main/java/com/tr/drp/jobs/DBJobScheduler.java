package com.tr.drp.jobs;

import com.tr.drp.service.file.JobContextHelper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DBJobScheduler {

    @Autowired
    private JobContextHelper jobContextHelper;

    @Autowired
    private Job inboundJob;

    @Autowired
    private JobLauncher jobLauncher;

    @Scheduled(cron = "${app.db-cron}")
    public void trigger() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("domain", "alj")
                .addString("jobId", jobContextHelper.generateNewId())
                .toJobParameters();
        jobLauncher.run(inboundJob, jobParameters);
    }
}
