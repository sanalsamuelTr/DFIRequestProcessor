package com.tr.drp.jobs.dfiinbound;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.tr.drp.common.model.DFIRequest;
import com.tr.drp.common.model.job.JobContext;
import com.tr.drp.config.SFTPProperties;
import com.tr.drp.jobs.JobRunner;
import com.tr.drp.service.file.LocalFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DFIInboundJobRunner implements JobRunner {
    private static final Logger log = LoggerFactory.getLogger(DFIInboundJobRunner.class);

    @Autowired
    RemoteFileTemplate<LsEntry> remoteFileTemplate;

    @Autowired
    LocalFilesService localFilesService;

    @Autowired
    SFTPProperties sftpProperties;

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

        LsEntry[] files = remoteFileTemplate.list(sftpProperties.getRemoteInboundDirectory());
        Set<String> fileNames = Arrays.stream(files).map(f -> f.getFilename()).collect(Collectors.toSet());
        List<DFIRequest> responsesAlreadyInDFI = requestWithoutResponse.stream()
                .filter(r -> fileNames.contains(r.getDfiRequestId() + ".end"))
                .filter(r -> fileNames.contains(r.getDfiRequestId() + ".out.csv.zip"))
                .collect(Collectors.toList());
        if (!responsesAlreadyInDFI.isEmpty()) {
            downloadAndUncompressFiles(responsesAlreadyInDFI);
        }
        return !responsesAlreadyInDFI.isEmpty();
    }

    private void downloadAndUncompressFiles(List<DFIRequest> responsesAlreadyInDFI) {
        log.info("Downloading responses: {}", responsesAlreadyInDFI);
        remoteFileTemplate.execute(session -> {
            for (DFIRequest dfiRequest : responsesAlreadyInDFI) {
                Path filePath = Paths.get(sftpProperties.getRemoteInboundDirectory(), dfiRequest.getDfiRequestId() + ".out.csv.zip");
                String lnxFilePath = filePath.toString().replace("\\", "/");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                session.read(lnxFilePath, baos);
                localFilesService.writeDFIOutPart(dfiRequest, baos.toByteArray());
            }
            return null;
        });
    }
}
