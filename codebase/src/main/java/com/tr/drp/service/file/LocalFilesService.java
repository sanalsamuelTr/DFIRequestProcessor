package com.tr.drp.service.file;

import java.nio.file.Path;
import java.util.List;

public interface LocalFilesService {
    Path getDFIProperties(String domain);
    Path getDFIPropertiesMap(String domain);
    List<Path> splitCSV(Path csv, int maxContentLines);
    Path newOutboundCSV();
}
