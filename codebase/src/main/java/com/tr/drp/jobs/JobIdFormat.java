package com.tr.drp.jobs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JobIdFormat {
    private final DateFormat dateFormat;

    public JobIdFormat(String jobIdPattern) {
        this.dateFormat = new SimpleDateFormat(jobIdPattern);
    }

    public String newJobId() {
        return dateFormat.format(new Date());
    }
}
