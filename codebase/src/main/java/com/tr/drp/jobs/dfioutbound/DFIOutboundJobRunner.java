package com.tr.drp.jobs.dfioutbound;

import com.tr.drp.common.model.DFIRequest;
import com.tr.drp.common.model.job.JobContext;
import com.tr.drp.common.model.job.JobType;
import com.tr.drp.jobs.JobRunner;
import com.tr.drp.service.dfi.DFIService;
import com.tr.drp.service.file.LocalFilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
public class DFIOutboundJobRunner implements JobRunner {

    @Autowired
    private LocalFilesService localFilesService;

    @Autowired
    private DFIService dfiService;

    @Value("${app.maxCSVContentLines}")
    private int maxCSVContentLines;

    @Override
    public boolean run(JobContext jobContext) {
        List<Path> parts = localFilesService.splitToDFIOutCSV(jobContext, maxCSVContentLines);
        int p = 0;
        for (Path partPath : parts) {
            p++;
            DFIRequest dfiRequest = DFIRequest.builder()
                    .csvFile(partPath)
                    .jobContext(jobContext)
                    .part(p)
                    .build();
            dfiService.sendRequest(dfiRequest);
        }
        localFilesService.createJobTrigger(JobContext.builder()
                .domain(jobContext.getDomain())
                .jobId(jobContext.getJobId())
                .jobType(JobType.DFI_PULL)
                .build());
        return true;
    }
}
