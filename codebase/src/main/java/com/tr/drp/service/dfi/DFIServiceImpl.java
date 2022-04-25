package com.tr.drp.service.dfi;

import com.tr.drp.common.model.DFIRequest;
import com.tr.drp.config.SFTPConfig;
import com.tr.drp.service.file.LocalFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class DFIServiceImpl implements DFIService {

    private static final Logger log = LoggerFactory.getLogger(DFIServiceImpl.class);

    private LocalFilesService localFilesService;

    private final SFTPConfig.SFTPGateway sftpGateway;


    public DFIServiceImpl(
            @Autowired LocalFilesService localFilesService,
            @Autowired SFTPConfig.SFTPGateway sftpGateway
    ) {
        this.localFilesService = localFilesService;
        this.sftpGateway = sftpGateway;
    }

    @Override
    public void sendRequest(DFIRequest dfiRequest) {
        log.info("Process Request: {}", dfiRequest);
        sendTargetCSV(dfiRequest);
        sendPropertyFile(dfiRequest);
        sendTriggerFile(dfiRequest);
    }


    private void sendPropertyFile(DFIRequest dfiRequest) {
        Path propertiesPath = localFilesService.getDFIProperties(dfiRequest.getJobContext().getDomain());
        sftpGateway.sendToSftp(propertiesPath.toFile(), dfiRequest.getDfiRequestId() + ".properties");
    }

    private void sendTargetCSV(DFIRequest dfiRequest) {
        sftpGateway.sendToSftp(dfiRequest.getCsvFile().toFile(), dfiRequest.getDfiRequestId() + ".csv");
    }

    private void sendTriggerFile(DFIRequest dfiRequest) {
        sftpGateway.sendToSftp(new byte[]{}, dfiRequest.getDfiRequestId() + ".trg");
    }
}
