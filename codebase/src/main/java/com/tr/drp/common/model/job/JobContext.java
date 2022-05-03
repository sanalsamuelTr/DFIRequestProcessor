package com.tr.drp.common.model.job;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobContext {
    private JobType jobType;
    private String jobId;
    private String domain;
}
