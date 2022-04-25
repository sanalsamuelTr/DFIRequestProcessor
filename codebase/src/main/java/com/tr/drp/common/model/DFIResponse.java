package com.tr.drp.common.model;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class DFIResponse {
    private Path responseFile;
}
