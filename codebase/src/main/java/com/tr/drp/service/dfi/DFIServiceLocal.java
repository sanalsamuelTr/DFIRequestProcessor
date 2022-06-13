package com.tr.drp.service.dfi;

import com.tr.drp.common.exception.ProcessorException;
import com.tr.drp.common.model.DFIRequest;
import com.tr.drp.service.file.LocalFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
@Profile("!sftp")
public class DFIServiceLocal implements DFIService {

    private static final Logger log = LoggerFactory.getLogger(DFIServiceLocal.class);


    @Value("${app.dfi-local.inbound}")
    private String dfiInBoundPath;
    @Value("${app.dfi-local.outbound}")
    private String dfiOutBoundPath;

    @Autowired
    private LocalFilesService localFilesService;

    @Override
    public void readBatchFromOut(Collection<String> paths, BiConsumer<String, byte[]> consumer) {
        for (String fileName : paths) {
            Path path = Paths.get(dfiInBoundPath, fileName);
            try {
                consumer.accept(fileName, Files.readAllBytes(path));
            } catch (IOException e) {
                throw new ProcessorException(e);
            }
        }
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
        try {
            Files.copy(propertiesPath, Paths.get(dfiOutBoundPath, dfiRequest.getDfiRequestId() + ".properties"));
        } catch (IOException e) {
            throw new ProcessorException(e);
        }
    }

    private void sendTargetCSV(DFIRequest dfiRequest) {
        try {
            Files.copy(dfiRequest.getCsvFile(), Paths.get(dfiOutBoundPath, dfiRequest.getDfiRequestId() + ".csv"));
        } catch (IOException e) {
            throw new ProcessorException(e);
        }
    }

    private void sendTriggerFile(DFIRequest dfiRequest) {
        try {
            Files.copy(new ByteArrayInputStream(new byte[]{}), Paths.get(dfiOutBoundPath, dfiRequest.getDfiRequestId() + ".trg"));
        } catch (IOException e) {
            throw new ProcessorException(e);
        }
    }

    @Override
    public Collection<String> listOutDir() {
        Path path = Paths.get(dfiInBoundPath);
        try {
            return Files.list(path).filter(p -> !p.toFile().isDirectory()).map(p -> p.getFileName().toString()).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new ProcessorException(e);
        }
    }
}
