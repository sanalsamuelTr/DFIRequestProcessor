package com.tr.drp.service.job;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobDescriptor {
    private JobType jobType;
    private String id;
    private String domain;

    public String toJobTriggerFileName() {
        return jobType.getName() + "_" + domain + "_" + id;
    }
}
