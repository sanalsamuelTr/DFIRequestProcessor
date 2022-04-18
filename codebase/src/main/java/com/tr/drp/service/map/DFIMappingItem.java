package com.tr.drp.service.map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DFIMappingItem {
    private String dfiInputField;
    private String dbQueryResponseColumn;
}
