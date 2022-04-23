package com.tr.drp.service.file;

import com.tr.drp.common.exception.ProcessorException;
import com.tr.drp.common.model.job.JobContext;
import com.tr.drp.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LocalFilesServiceImpl implements LocalFilesService {

    private static final Logger log = LoggerFactory.getLogger(LocalFilesServiceImpl.class);
    public static final String ERROR_FILE_SUFFIX = ".error";

    @Value("${app.path.domain}")
    private String baseDomainPath;
    @Value("${app.file.dfi-properties}")
    private String dfiPropertiesFileName;
    @Value("${app.file.dfi-properties-map}")
    private String dfiPropertiesMapFileName;
    @Value("${app.path.output}")
    private String outputBasePath;
    @Value("${app.path.job}")
    private String jobTriggerPath;

    @Autowired
    private JobContextHelper jobContextHelper;

    private Set<String> triggerFilesBlackList = new HashSet<>();

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
    public Path dbOutCSV(String domain, String id) {
        return Paths.get(outputBasePath, domain, id, "db_out.csv");
    }

    @Override
    public Path createJobTrigger(JobContext jobContext) {
        Path path = Paths.get(jobTriggerPath, jobContextHelper.getJobTriggerFileName(jobContext));
        createFile(path);
        return path;
    }
    @Override
    public List<JobContext> getJobContextsFromTriggerFiles() {
        try {
            Set<String> allFilesNames = Files.list(Paths.get(jobTriggerPath))
                    .filter(f -> !Files.isDirectory(f))
                    .map(f -> f.getFileName().toString())
                    .collect(Collectors.toSet());
            Set<String> errorFiles = allFilesNames.stream().filter(f -> f.endsWith(ERROR_FILE_SUFFIX))
                    .map(f -> StringUtils.remSuffix(f, ERROR_FILE_SUFFIX)).collect(Collectors.toSet());
            return allFilesNames.stream()
                    .filter(jobContextHelper::isTriggerFileName)
                    .filter(f -> !triggerFilesBlackList.contains(f))
                    .filter(f -> !errorFiles.contains(f))
                    .map(f -> jobContextHelper.fromFileName(f))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ProcessorException("Fail to list job triggers path: " + jobTriggerPath, e);
        }
    }

    @Override
    public void removeJobTrigger(JobContext jobContext) {
        Path path = Paths.get(jobTriggerPath, jobContextHelper.getJobTriggerFileName(jobContext));
        log.info("Removing job trigger: {}", path);
        try {
            Files.delete(path);
        } catch (IOException e) {
            log.error("Can't delete trigger file, which will be blacklisted for this app run: {}", path);
            triggerFilesBlackList.add(path.getFileName().toString());
        }
    }

    @Override
    public void createJobTriggerErrorFile(JobContext jobContext) {
        String fileName = jobContextHelper.getJobTriggerFileName(jobContext) + ERROR_FILE_SUFFIX;
        Path filePath = Paths.get(jobTriggerPath, fileName);
        try {

            createFile(filePath);
        } catch (ProcessorException e) {
            log.error("Can't create job trigger error file: {}", filePath);
        }
    }



    private void createFile(Path file) {
        try {
            Files.createDirectories(file.getParent());
            Files.createFile(file);
        } catch (IOException e) {
            throw new ProcessorException("File creation: " + file, e);
        }
    }
}
