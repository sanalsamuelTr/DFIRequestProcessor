package com.tr.drp.service.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
public class LocalFilesServiceImpl implements LocalFilesService {

    private String baseDomainPath;
    private String dfiPropertiesFileName;
    private String dfiPropertiesMapFileName;

    public LocalFilesServiceImpl(
            @Value("${app.path.domain}")
            String baseDomainPath,
            @Value("${app.file.dfi-properties}")
            String dfiPropertiesFileName,
            @Value("${app.file.dfi-properties-map}")
            String dfiPropertiesMapFileName) {
        this.baseDomainPath = baseDomainPath;
        this.dfiPropertiesFileName = dfiPropertiesFileName;
        this.dfiPropertiesMapFileName = dfiPropertiesMapFileName;
    }

    @Override
    public Path getDFIProperties(String domain) {
        return Paths.get(baseDomainPath, domain, dfiPropertiesFileName);
    }

    @Override
    public Path getDFIPropertiesMap(String domain) {
        return Paths.get(baseDomainPath, domain, dfiPropertiesMapFileName);
    }

    @Override
    public List<Path> splitCSV(Path csv, int maxContentLines) {
        //TODO: implement csv file splitting
        return Arrays.asList(csv);
    }

    @Override
    public Path newOutboundCSV() {
        return Paths.get("out.csv");
    }
}
