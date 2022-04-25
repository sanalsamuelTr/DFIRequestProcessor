package com.tr.drp.jobs;

import com.tr.drp.common.model.job.JobContext;

public interface JobRunner {
    boolean run(JobContext jobContext);
}
