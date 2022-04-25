package com.tr.drp.service.file;

import com.tr.drp.common.exception.ProcessorException;
import com.tr.drp.common.model.job.JobContext;
import com.tr.drp.common.model.job.JobType;
import com.tr.drp.jobs.JobIdFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class JobContextHelper {
    @Autowired
    private JobIdFormat jobIdFormat;
    private final Pattern fileNamePattern;

    public JobContextHelper() {
        String jobTypesEnumeration = Arrays.stream(JobType.values()).map(e -> e.getName())
                .collect(Collectors.joining("|"));
        fileNamePattern = Pattern.compile("([^_]+)_(" + jobTypesEnumeration + ")_(.+).trg");
    }

    public boolean isTriggerFileName(String fileName) {
        return fileNamePattern.matcher(fileName).matches();
    }

    JobContext generateNewJob(JobType jobType, String domain) {
        return JobContext.builder()
                .jobType(jobType)
                .jobId(generateNewId())
                .domain(domain)
                .build();
    }

    public JobContext fromFileName(String fileName) {
        Matcher matcher = fileNamePattern.matcher(fileName);
        if (matcher.matches()) {
            String domain = matcher.group(1);
            JobType type = JobType.byName(matcher.group(2));
            String id = matcher.group(3);
            return JobContext.builder().jobId(id).jobType(type).domain(domain).build();
        } else {
            throw new ProcessorException("Unsupported job trigger file name: " + fileName);
        }
    }

    public String getJobTriggerFileName(JobContext jobContext) {
        return jobContext.getDomain() + "_" + jobContext.getJobType().getName() + "_" + jobContext.getJobId() + ".trg";
    }

    public String generateNewId() {
        return jobIdFormat.newJobId();
    }
}
