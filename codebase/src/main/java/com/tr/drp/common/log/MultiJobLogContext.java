package com.tr.drp.common.log;

import com.tr.drp.common.model.job.JobContext;

public class MultiJobLogContext {
    private static ThreadLocal<JobContext>threadJobContext = new InheritableThreadLocal<>();

    public static void setJobContext(JobContext jobContext) {
        threadJobContext.set(jobContext);
    }

    public static JobContext getJobContext() {
        return threadJobContext.get();
    }
}
