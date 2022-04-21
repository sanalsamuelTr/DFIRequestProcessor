package com.tr.drp.service.job;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class JobBuilder {
    private final DateFormat idFormatPattern;
    private Pattern fileNamePattern;

    public JobBuilder(@Value("${app.jobid-pattern}") String idPattern) {
        idFormatPattern = new SimpleDateFormat(idPattern);
        String jobTypesEnumeration = Arrays.stream(JobType.values()).map(e -> e.getName())
                .collect(Collectors.joining("|"));
        fileNamePattern = Pattern.compile("(" + jobTypesEnumeration + ")_([^_]+)_(.+)");
    }

    JobDescriptor generateNewJob(JobType jobType, String domain) {
        return JobDescriptor.builder()
                .jobType(jobType)
                .id(generateNewId())
                .domain(domain)
                .build();
    }

    JobDescriptor fromFileName(String fileName) {
        Matcher matcher = fileNamePattern.matcher(fileName);
        if (matcher.matches()) {
            JobType type = JobType.byName(matcher.group(1));
            String domain = matcher.group(2);
            String id = matcher.group(3);
            return JobDescriptor.builder().id(id).jobType(type).domain(domain).build();
        }
        return null;
    }

    public String generateNewId() {
        return idFormatPattern.format(new Date());
    }
}
