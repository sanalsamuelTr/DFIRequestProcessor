package com.tr.drp.jobs.dfiinbound;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.tr.drp.common.model.DFIRequest;
import com.tr.drp.common.model.job.JobContext;
import com.tr.drp.jobs.JobRunner;
import com.tr.drp.service.dfi.DFIService;
import com.tr.drp.service.file.LocalFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DFIInboundJobRunner implements JobRunner {
    private static final Logger log = LoggerFactory.getLogger(DFIInboundJobRunner.class);

    @Autowired
    LocalFilesService localFilesService;

    @Value("${app.compressed:true}")
    private boolean outCompressed;

    @Autowired
    DFIService dfiService;

    @Override
    public boolean run(JobContext jobContext) {
        if (localFilesService.checkDFIOutExist(jobContext)) {
            return true;
        }
        List<DFIRequest> dfiRequestList = localFilesService.getDFIRequests(jobContext);
        List<DFIRequest> requestWithoutResponse = dfiRequestList.stream().filter(r -> r.getDfiResponse() == null).collect(Collectors.toList());
        if (requestWithoutResponse.isEmpty()) {
            log.info("Merging dfi out parts {} for: {}", dfiRequestList.size(), jobContext);
            localFilesService.collectDFIOutCSVs(dfiRequestList);
            return true;
        } else {
            if (loadRemoteResponses(requestWithoutResponse) && requestWithoutResponse.stream().filter(r -> r.getDfiResponse() == null).count() == 0) {
                log.info("Merging dfi out parts {} for: {}", dfiRequestList.size(), jobContext);
                localFilesService.collectDFIOutCSVs(dfiRequestList);
                return true;
            }
        }
        return false;
    }

    private boolean loadRemoteResponses(List<DFIRequest> requestWithoutResponse) {
        log.info("Check dfi responses parts: {}", requestWithoutResponse.stream().map(r -> r.getDfiRequestId()).collect(Collectors.toList()));
        if (requestWithoutResponse.isEmpty()) {
            return false;
        }

        Collection<String> fileNames = dfiService.listOutDir();
        List<DFIRequest> responsesAlreadyInDFI = requestWithoutResponse.stream()
                .filter(r -> fileNames.contains(r.getDfiRequestId() + ".end"))
                .filter(r -> fileNames.contains(r.getDfiRequestId() + ".out.csv" + (outCompressed ? ".zip" : "")))
                .collect(Collectors.toList());
        if (!responsesAlreadyInDFI.isEmpty()) {
            downloadAndUncompressFiles(responsesAlreadyInDFI);
        }
        return !responsesAlreadyInDFI.isEmpty();
    }

    private void downloadAndUncompressFiles(List<DFIRequest> responsesAlreadyInDFI) {
        log.info("Downloading responses: {}", responsesAlreadyInDFI);
        Map<String, DFIRequest> dataFileNameToRequest = responsesAlreadyInDFI.stream().collect(Collectors.toMap(
                r -> r.getDfiRequestId() + ".out.csv" + (outCompressed ? ".zip" : ""),
                r -> r
        ));
        dfiService.readBatchFromOut(dataFileNameToRequest.keySet(), (path, data) -> {
            DFIRequest request = dataFileNameToRequest.get(path);
            if (outCompressed) {
                localFilesService.writeDFIOutPartCompressed(request, data);
            } else {
                localFilesService.writeDFIOutPart(request, data);
            }
        });
    }
}
