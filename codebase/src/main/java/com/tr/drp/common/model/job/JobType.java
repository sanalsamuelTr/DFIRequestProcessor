package com.tr.drp.common.model.job;

import com.tr.drp.common.exception.ProcessorException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum  JobType {
    DB_LOAD("db-load"), DFI_PUSH("dfi-push"), DFI_PULL("dfi-pull");
    private String name;

    public String getName() {
        return name;
    }

    public static JobType byName(String name) {
        for (JobType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new ProcessorException("Unknown job type: " + name);
    }
}
