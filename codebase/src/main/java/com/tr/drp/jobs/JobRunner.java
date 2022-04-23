package com.tr.drp.jobs;

import com.tr.drp.common.model.job.JobContext;

public interface JobRunner {
    public void run(JobContext jobContext);
}
