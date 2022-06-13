package com.tr.drp.service.dfi;

import com.tr.drp.common.model.DFIRequest;

import java.util.Collection;
import java.util.function.BiConsumer;

public interface DFIService {
    void readBatchFromOut(Collection<String> paths, BiConsumer<String, byte[]> consumer);

    void sendRequest(DFIRequest dfiRequest);
    Collection<String> listOutDir();
}
