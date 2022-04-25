package com.tr.drp.jobs;

import com.tr.drp.common.exception.ProcessorException;
import com.tr.drp.common.model.job.JobContext;
import com.tr.drp.common.model.job.JobType;
import com.tr.drp.jobs.dfiinbound.DFIInboundJobRunner;
import com.tr.drp.jobs.dfioutbound.DFIOutboundJobRunner;
import com.tr.drp.service.file.LocalFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class FileTriggerJobScheduler {
    private static final Logger log = LoggerFactory.getLogger(FileTriggerJobScheduler.class);

    @Autowired
    DFIOutboundJobRunner dfiOutboundJobRunner;

    @Autowired
    DFIInboundJobRunner dfiInboundJobRunner;

    @Autowired
    private LocalFilesService localFilesService;

    @Scheduled(initialDelay = 5000, fixedDelayString = "#{${app.file-trigger.check-period-sec} * 1000}")
    public void trigger() {
        int completed = 0;
        while ((completed = run()) != 0) {
            pause();
            log.info("Auto reschedule immediately jobs run after {} jobs success completed.", completed);
        }
    }

    private void pause() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    private int run() {
        List<JobContext> jobContexts = localFilesService.getJobContextsFromTriggerFiles();
        log.info("JobsToProcess: {}", jobContexts);
        List<JobContext> errorJobContexts = new ArrayList<>();
        List<JobContext> endedJobs = jobContexts.stream()
                .filter(j -> runJob(j, errorJobContexts))
                .collect(Collectors.toList());
        if (!errorJobContexts.isEmpty()) {
            log.error("ERROR jobs: {}", errorJobContexts);
        }
        return endedJobs.size();
    }

    private boolean runJob(JobContext jobContext, List<JobContext> errorJobContexts) {
        try {
            log.info("Process job: {}", jobContext);
            JobRunner jobRunner = getJobRunner(jobContext.getJobType());
            if (jobRunner.run(jobContext)) {
                localFilesService.removeJobTrigger(jobContext);
                return true;
            }
        } catch (ProcessorException e) {
            log.error("Fail to process job: {}", jobContext, e);
            localFilesService.createJobTriggerErrorFile(jobContext);
            errorJobContexts.add(jobContext);
        }
        return false;
    }

    private JobRunner getJobRunner(JobType jobType) {
        switch (jobType) {
            case DFI_PUSH:
                return dfiOutboundJobRunner;
            case DFI_PULL:
                return dfiInboundJobRunner;
            default:
                throw new ProcessorException("Unsupported job type: " + jobType);
        }
    }
}
