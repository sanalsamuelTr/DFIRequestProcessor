package com.tr.drp.service.dfi;

import com.tr.drp.common.model.DFIRequest;
import com.tr.drp.common.model.DFIResponse;

import java.util.concurrent.Future;

public interface DFIService {
    Future<DFIResponse> processRequest(DFIRequest dfiRequest);
}
