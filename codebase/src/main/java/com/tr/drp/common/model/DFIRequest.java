package com.tr.drp.common.model;

import com.tr.drp.common.model.job.JobContext;
import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class DFIRequest {
    private DFIResponse dfiResponse;
    private Path csvFile;
    private JobContext jobContext;
    private int part;

    public String getDfiRequestId() {
        return jobContext.getDomain() + "_" + jobContext.getJobId() + "_" + part;
    }
}
