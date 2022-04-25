package com.tr.drp.common.utils;

import java.util.ArrayList;
import java.util.List;

public class CollectionsUtils {
    public <T> List<List<T>> group(List<T> list, int maxCount) {
        if (list == null) {
            return null;
        }
        List<List<T>> res = new ArrayList<>();
        int batchCount = list.size() / maxCount + 1;
        return res;
    }
}