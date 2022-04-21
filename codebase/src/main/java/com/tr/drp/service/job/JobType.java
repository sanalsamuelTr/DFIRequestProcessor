package com.tr.drp.service.job;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum  JobType {
    DB_LOAD("db_load"), DFI_PUSH("dfi_push"), DFI_PULL("dfi_pull");
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
        return null;
    }
}
