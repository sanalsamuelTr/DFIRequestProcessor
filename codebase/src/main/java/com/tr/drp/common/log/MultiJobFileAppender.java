package com.tr.drp.common.log;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.Encoder;
import com.tr.drp.common.model.job.JobContext;
import com.tr.drp.service.file.LocalFilesService;

import java.util.HashMap;
import java.util.Map;

public class MultiJobFileAppender extends AppenderBase {
    private Map<JobContext, FileAppender> jobFileAppender = new HashMap<>();

    private Encoder encoder;
    private String filePath;

    public Encoder getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder encoder) {
        this.encoder = encoder;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected void append(Object o) {
        JobContext jobContext = MultiJobLogContext.getJobContext();
        if (jobContext != null) {
            appendToJob(o, jobContext);
        }
    }
    private void appendToJob(Object o, JobContext jobContext) {
        FileAppender fileAppender = jobFileAppender.computeIfAbsent(jobContext, jc -> newJobAppender(jc));
        fileAppender.doAppend(o);
    }

    private FileAppender newJobAppender(JobContext jobContext) {

        FileAppender fileAppender = new FileAppender();
        fileAppender.setFile(LocalFilesService.getJobContextPath(jobContext).resolve(filePath).toString());
        fileAppender.setEncoder(getEncoder());
        fileAppender.setName(jobContext.getJobId());
        fileAppender.setAppend(true);
        fileAppender.setContext(getContext());
        fileAppender.start();
        return fileAppender;
    }

    @Override
    public void stop() {
        jobFileAppender.values().stream().forEach(FileAppender::stop);
    }
}
