package com.tr.drp.service.file;

import com.tr.drp.common.exception.ProcessorException;
import com.tr.drp.service.job.JobBuilder;
import com.tr.drp.service.job.JobDescriptor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
public class LocalFilesServiceImpl implements LocalFilesService {

    private String baseDomainPath;
    private String dfiPropertiesFileName;
    private String dfiPropertiesMapFileName;
    private String outputBasePath;
    private String jobTriggerPath;

    public LocalFilesServiceImpl(
            @Value("${app.path.domain}")
            String baseDomainPath,
            @Value("${app.file.dfi-properties}")
            String dfiPropertiesFileName,
            @Value("${app.file.dfi-properties-map}")
            String dfiPropertiesMapFileName,
            @Value("${app.path.output}")
            String outputBasePath,
            @Value("${app.path.job}")
            String jobTriggerPath) {
        this.baseDomainPath = baseDomainPath;
        this.dfiPropertiesFileName = dfiPropertiesFileName;
        this.dfiPropertiesMapFileName = dfiPropertiesMapFileName;
        this.outputBasePath = outputBasePath;
        this.jobTriggerPath = jobTriggerPath;
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
    public Path newOutboundDFICSV(String domain, String id) {
        return Paths.get(outputBasePath, domain, "dfi_out.csv");
    }

    @Override
    public Path createJobTrigger(JobDescriptor jobDescriptor) {
        Path path = Paths.get(jobTriggerPath, jobDescriptor.toJobTriggerFileName());
        createFile(path);
        return path;
    }

    private void createFile(Path file) {
        try {
            Files.createDirectories(file.getParent());
            Files.createFile(file);
        } catch (IOException e) {
            throw new ProcessorException("File createion: " + file, e);
        }
    }
}
