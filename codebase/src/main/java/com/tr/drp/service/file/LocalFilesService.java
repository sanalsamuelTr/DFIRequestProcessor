package com.tr.drp.service.file;

import com.tr.drp.Main;
import com.tr.drp.common.model.DFIRequest;
import com.tr.drp.common.model.job.JobContext;

import java.nio.file.Path;
import java.util.List;

public interface LocalFilesService {
    Path getDFIProperties(String domain);

    Path getDFIPropertiesMap(String domain);

    List<DFIRequest> getDFIRequests(JobContext jobContext);

    boolean checkDFIOutExist(JobContext jobContext);

    List<Path> splitToDFIOutCSV(JobContext jobContext, int maxContentLines);

    Path dbOutCSV(String domain, String id);

    Path createJobTrigger(JobContext jobContext);

    List<JobContext> getJobContextsFromTriggerFiles();

    void removeJobTrigger(JobContext jobContext);

    void createJobTriggerErrorFile(JobContext jobContext);

    void writeDFIOutPart(DFIRequest request, byte[] zip);

    void collectDFIOutCSVs(List<DFIRequest> requests);

    static Path getJobContextPath(JobContext jobContext) {
        LocalFilesService filesService = Main.getApplicationContext().getBean(LocalFilesService.class);
       return filesService.getJobOutPath(jobContext);
    }

    Path getJobOutPath(JobContext jobContext);
}
