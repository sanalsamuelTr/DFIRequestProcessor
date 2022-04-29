package com.tr.drp.service.file;

import com.google.common.collect.Lists;
import com.tr.drp.common.exception.ProcessorException;
import com.tr.drp.common.model.DFIRequest;
import com.tr.drp.common.model.DFIResponse;
import com.tr.drp.common.model.job.JobContext;
import com.tr.drp.common.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Service
public class LocalFilesServiceImpl implements LocalFilesService {

    private static final Logger log = LoggerFactory.getLogger(LocalFilesServiceImpl.class);
    public static final String ERROR_FILE_SUFFIX = ".error";

    @Value("${app.path.domain.config}")
    private String baseDomainPath;
    @Value("${app.file.dfi-properties}")
    private String dfiPropertiesFileName;
    @Value("${app.file.dfi-properties-map}")
    private String dfiPropertiesMapFileName;
    @Value("${app.path.output}")
    private String outputBasePath;
    @Value("${app.path.job}")
    private String jobTriggerPath;
    @Value("${app.maxCSVContentLines}")
    private int maxCSVContentLines;
    @Value("${app.path.domain.job.dfi-out}")
    private String dfiOutPath;
    @Value("${app.path.domain.job.dfi-in}")
    private String dfiInPath;

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
    public List<DFIRequest> getDFIRequests(JobContext jobContext) {
        Path path = Paths.get(outputBasePath, jobContext.getDomain(), jobContext.getJobId(), dfiOutPath);
        try {
            return Files.list(path)
                    .filter(f -> !Files.isDirectory(f))
                    .filter(f -> isDFIOUTPartFileName(f.getFileName().toString()))
                    .map(f -> {
                                int part = getDFIOUTPartNum(f.getFileName().toString());
                                DFIRequest r = DFIRequest.builder()
                                        .jobContext(jobContext)
                                        .part(part)
                                        .dfiResponse(generateDfiResponse(jobContext, part))
                                        .csvFile(f)
                                        .build();
                                return r;
                            }
                    )
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ProcessorException("Can't check dfi out requests directory: " + path, e);
        }
    }

    @Override
    public boolean checkDFIOutExist(JobContext jobContext) {
        Path outPath = getDFIOutPath(jobContext);
        return Files.exists(outPath);
    }

    private DFIResponse generateDfiResponse(JobContext jobContext, int part) {
        Path path = Paths.get(outputBasePath, jobContext.getDomain(), jobContext.getJobId(), dfiInPath, "dfi_in_" + part + ".csv");
        if (!Files.exists(path)) {
            return null;
        }
        return DFIResponse.builder().responseFile(path).build();
    }

    private final Pattern dfiOutPartFileNamePattern = Pattern.compile("dfi_out_(\\d+)[.]csv");

    private boolean isDFIOUTPartFileName(String fileName) {
        return dfiOutPartFileNamePattern.matcher(fileName).matches();
    }

    private int getDFIOUTPartNum(String fileName) {
        Matcher matcher = dfiOutPartFileNamePattern.matcher(fileName);
        matcher.matches();
        return Integer.parseInt(matcher.group(1));
    }

    @Override
    public List<Path> splitToDFIOutCSV(JobContext jobContext, int maxContentLines) {
        Path path = dbOutCSV(jobContext.getDomain(), jobContext.getJobId());
        List<String> csvLines = null;
        if (!Files.exists(path)) {
            new ProcessorException("No content file: " + path);
        }
        try {
            csvLines = Files.readAllLines(path);
        } catch (IOException e) {
            new ProcessorException("Can't read csv content: " + path, e);
        }
        List<List<String>> parts = splitCSVLinesIntoParts(csvLines, maxContentLines);
        int p = 0;
        List<Path> fileParts = new ArrayList<>();
        try {
            Files.createDirectories(getDFIOutPartPath(jobContext, 0).getParent());
            for (List<String> part : parts) {
                p++;
                Path partFilePath = getDFIOutPartPath(jobContext, p);
                fileParts.add(partFilePath);
                Files.deleteIfExists(partFilePath);
                FileUtils.writeLines(partFilePath.toFile(), "UTF-8", part, null);
            }
        } catch (IOException e) {
            throw new ProcessorException("Can't split dfi out for job: " + jobContext, e);
        }
        return fileParts;
    }

    public Path getDFIOutPartPath(JobContext jobContext, int part) {
        return Paths.get(outputBasePath, jobContext.getDomain(), jobContext.getJobId(), dfiOutPath, "dfi_out_" + part + ".csv");
    }

    private List<List<String>> splitCSVLinesIntoParts(List<String> csvLines, int maxContentLines) {
        List<List<String>> parts;
        List<String> headLines = getCSVHeadLines(csvLines);
        headLines.addAll(0, getServiceHeads());
        List<String> csvContentLines = csvLines.stream().filter(l -> !isCSVHeadLine(l)).collect(Collectors.toList());
        parts = Lists.partition(csvContentLines, maxContentLines);
        for (List<String> part : parts) {
            part.addAll(0, headLines);
        }
        return parts;
    }

    private List<String> getServiceHeads() {
        return Arrays.asList("#TaxCalculationService,CalculateTax");
    }

    private List<String> getCSVHeadLines(List<String> lines) {
        List<String> headLines = new ArrayList<>();
        for (String line : lines) {
            if (isCSVHeadLine(line)) {
                headLines.add(line);
            } else {
                break;
            }
        }
        return headLines;
    }

    private boolean isCSVHeadLine(String line) {
        return line.startsWith("#");
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
        if (!Files.exists(Paths.get(jobTriggerPath))) {
            return new ArrayList<>();
        }
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

    @Override
    public void writeDFIOutPart(DFIRequest request, byte[] zip) {
        Path zipPath = Paths.get(outputBasePath, request.getJobContext().getDomain(), request.getJobContext().getJobId(), dfiInPath,
                request.getDfiRequestId() + ".zip");
        try {
            Files.createDirectories(zipPath.getParent());
            Files.write(zipPath, zip);
        } catch (IOException e) {
            throw new ProcessorException("Can't write zip to: " + zipPath, e);
        }
        ByteArrayOutputStream unzbaos = new ByteArrayOutputStream();
        try {
            StreamUtils.copy(new GZIPInputStream(new ByteArrayInputStream(zip)), unzbaos);
        } catch (IOException e) {
            throw new ProcessorException("Can't unzip: " + zipPath, e);
        }
        Path unzipPath = Paths.get(outputBasePath, request.getJobContext().getDomain(), request.getJobContext().getJobId(), dfiInPath,
                "dfi_in_" + request.getPart() + ".csv");
        try {
            Files.write(unzipPath, unzbaos.toByteArray());
        } catch (IOException e) {
            throw new ProcessorException("Can't write unzipped: " + unzipPath);
        }
        request.setDfiResponse(DFIResponse.builder().responseFile(unzipPath).build());
    }

    @Override
    public void collectDFIOutCSVs(List<DFIRequest> requests) {
        if (requests.isEmpty()) {
            throw new ProcessorException("Empty requests");
        }
        JobContext jobContext = requests.get(0).getJobContext();
        List<List<String>> contents = requests.stream().map(r -> {
            try {
                return Files.readAllLines(r.getDfiResponse().getResponseFile());
            } catch (IOException e) {
                throw new ProcessorException("Can't read file: " + r.getDfiResponse().getResponseFile());
            }
        }).collect(Collectors.toList());
        List<String> headers = contents.get(0).stream().filter(l -> l.startsWith("#")).collect(Collectors.toList());
        List<String> allContentLines = contents.stream().flatMap(c -> c.stream().filter(l -> !l.startsWith("#"))).collect(Collectors.toList());
        allContentLines.addAll(0, headers);
        Path outPath = getDFIOutPath(jobContext);
        try {
            FileUtils.writeLines(outPath.toFile(), allContentLines);
        } catch (IOException e) {
            throw new ProcessorException("Can't write file: " + outPath);
        }
    }

    private Path getDFIOutPath(JobContext jobContext) {
        Path outPath = Paths.get(outputBasePath, jobContext.getDomain(), jobContext.getJobId(), "dfi_out" + ".csv");
        return outPath;
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
