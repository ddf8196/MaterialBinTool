package com.ddf.materialbintool.main.compiler;

import java.util.LinkedHashMap;
import java.util.Map;

public class Defines {
    private final Map<String, String> defines = new LinkedHashMap<>();

    public void addDefine(String key) {
        defines.put(key, "");
    }

    public void addDefine(String key, String value) {
        defines.put(key, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : defines.entrySet()) {
            sb.append(entry.getKey());
            if (!entry.getValue().isEmpty()) {
                sb.append("=").append(entry.getValue());
            }
            sb.append(";");
        }
        return sb.toString();
    }
}
