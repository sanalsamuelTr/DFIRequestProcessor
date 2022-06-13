package com.tr.drp.service.dfi;

import com.jcraft.jsch.ChannelSftp;
import com.tr.drp.common.model.DFIRequest;
import com.tr.drp.config.SFTPConfig;
import com.tr.drp.config.SFTPProperties;
import com.tr.drp.service.file.LocalFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
@Profile("sftp")
public class DFIServiceSFTP implements DFIService {

    private static final Logger log = LoggerFactory.getLogger(DFIServiceSFTP.class);

    private LocalFilesService localFilesService;

    private final SFTPConfig.SFTPGateway sftpGateway;

    RemoteFileTemplate<ChannelSftp.LsEntry> remoteFileTemplate;

    SFTPProperties sftpProperties;


    public DFIServiceSFTP(
            @Autowired LocalFilesService localFilesService,
            @Autowired SFTPConfig.SFTPGateway sftpGateway,
            @Autowired RemoteFileTemplate<ChannelSftp.LsEntry> remoteFileTemplate,
            @Autowired SFTPProperties sftpProperties

    ) {
        this.localFilesService = localFilesService;
        this.sftpGateway = sftpGateway;
        this.remoteFileTemplate = remoteFileTemplate;
        this.sftpProperties = sftpProperties;
    }

    @Override
    public Collection<String> listOutDir() {
        return Arrays.stream(remoteFileTemplate.list(sftpProperties.getRemoteInboundDirectory()))
                .map(f -> f.getFilename()).collect(Collectors.toSet());
    }

    @Override
    public void readBatchFromOut(Collection<String> paths, BiConsumer<String, byte[]> consumer) {
        remoteFileTemplate.execute(session -> {
            for (String path : paths) {
                String fullPath = sftpProperties.getRemoteInboundDirectory() + path;
                String lnxFilePath = fullPath.toString().replace("\\", "/");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                session.read(lnxFilePath, baos);
                consumer.accept(path, baos.toByteArray());
            }
            return null;
        });
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
