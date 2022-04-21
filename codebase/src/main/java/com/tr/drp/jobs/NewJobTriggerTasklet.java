package com.tr.drp.jobs;

import com.tr.drp.service.file.LocalFilesService;
import com.tr.drp.service.job.JobDescriptor;
import com.tr.drp.service.job.JobType;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.Nullable;

public class NewJobTriggerTasklet implements Tasklet {
    private JobType jobType;
    private LocalFilesService localFilesService;

    public NewJobTriggerTasklet(JobType jobType, LocalFilesService localFilesService) {
        this.jobType = jobType;
        this.localFilesService = localFilesService;
    }

    @Nullable
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String jobId = chunkContext.getStepContext().getJobParameters().get("jobId").toString();
        String domain = chunkContext.getStepContext().getJobParameters().get("domain").toString();
        localFilesService.createJobTrigger(JobDescriptor.builder()
                .id(jobId)
                .domain(domain)
                .jobType(jobType)
                .build());
        return RepeatStatus.FINISHED;
    }
}
