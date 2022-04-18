package com.tr.drp.service.file;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
public class LocalFilesServiceImpl implements LocalFilesService {
    @Override
    public List<Path> splitCSV(Path csv, int maxContentLines) {
        //TODO: implement csv file splitting
        return Arrays.asList(csv);
    }

    @Override
    public Path newOutboundCSV() {
        return Paths.get("out.csv");
    }
}
