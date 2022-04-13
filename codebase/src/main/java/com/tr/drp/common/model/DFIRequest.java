package com.tr.drp.common.model;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class DFIRequest {
    private String id;
    private Path csvFile;
    private Path propertiesFile;
}
