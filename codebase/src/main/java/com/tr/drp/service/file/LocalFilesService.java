package com.tr.drp.service.file;

import com.tr.drp.common.model.job.JobContext;

import java.nio.file.Path;
import java.util.List;

public interface LocalFilesService {
    Path getDFIProperties(String domain);
    Path getDFIPropertiesMap(String domain);
    List<Path> splitCSV(Path csv, int maxContentLines);
    Path dbOutCSV(String domain, String id);
    Path createJobTrigger(JobContext jobContext);

    List<JobContext> getJobContextsFromTriggerFiles();

    void removeJobTrigger(JobContext jobContext);

    void createJobTriggerErrorFile(JobContext jobContext);
}
