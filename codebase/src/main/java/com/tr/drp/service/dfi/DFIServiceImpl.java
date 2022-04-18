package com.tr.drp.service.dfi;

import com.tr.drp.common.model.DFIRequest;
import com.tr.drp.common.model.DFIResponse;
import com.tr.drp.service.file.LocalFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class DFIServiceImpl implements DFIService {

    private static final Logger log = LoggerFactory.getLogger(DFIServiceImpl.class);

    private LocalFilesService localFilesService;

    @Value("${app.maxCSVContentLines}")
    private int maxCSVContentLines;

    public DFIServiceImpl(@Autowired LocalFilesService localFilesService) {
        this.localFilesService = localFilesService;
    }

    @Override
    @Async
    public Future<DFIResponse> processRequest(DFIRequest dfiRequest) {
        log.info("Process Request: {}", dfiRequest);
        List<Path> csvs = localFilesService.splitCSV(dfiRequest.getCsvFile(), maxCSVContentLines);

        return null;
    }
}
