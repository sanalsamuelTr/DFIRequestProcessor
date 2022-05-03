package com.tr.drp.common.utils;

import java.util.Collection;
import java.util.Collections;

public class CollectionsUtils {
    public static <T> Collection<T> safe(Collection<T> c) {
        return c == null ? Collections.EMPTY_LIST : c;
    }
}