package com.tr.drp.jobs;

import com.tr.drp.common.exception.ProcessorException;
import com.tr.drp.common.log.MultiJobLogContext;
import com.tr.drp.common.model.job.JobContext;
import com.tr.drp.common.model.job.JobType;
import com.tr.drp.service.file.JobContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DBJobRunnerFactory {

    private static final Logger log = LoggerFactory.getLogger(DBJobRunnerFactory.class);

    @Autowired
    private JobContextHelper jobContextHelper;

    @Autowired
    private Job inboundJob;

    @Autowired
    private JobLauncher jobLauncher;

    public Runnable createRunner(String domain) {
        return () -> {
            try {
                trigger(domain);
            } catch (Exception e) {
                throw new ProcessorException("Error job processing", e);
            }
        };
    }

    public void trigger(String domain) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobContext jobContext = JobContext.builder()
                .jobId(jobContextHelper.generateNewId())
                .domain(domain)
                .jobType(JobType.DB_LOAD)
                .build();
        MultiJobLogContext.setJobContext(jobContext);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("domain", domain)
                .addString("jobId", jobContext.getJobId())
                .toJobParameters();
        log.info("Starting db job {}", jobContext);
        jobLauncher.run(inboundJob, jobParameters);
    }
}
