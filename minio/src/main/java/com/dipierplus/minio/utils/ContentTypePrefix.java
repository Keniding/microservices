package com.dipierplus.minio.utils;

import java.util.Arrays;

public enum ContentTypePrefix {
    IMAGE("image/", "img_"),
    VIDEO("video/", "vid_"),
    PDF("application/pdf", "pdf_"),
    AUDIO("audio/", "aud_"),
    TEXT("text/", "txt_"),
    WORD("application/msword", "doc_"),
    WORD_X("application/vnd.openxmlformats-officedocument.wordprocessingml", "doc_"),
    EXCEL("application/vnd.ms-excel", "xls_"),
    EXCEL_X("application/vnd.openxmlformats-officedocument.spreadsheetml", "xls_"),
    POWERPOINT("application/vnd.ms-powerpoint", "ppt_"),
    POWERPOINT_X("application/vnd.openxmlformats-officedocument.presentationml", "ppt_");

    private final String contentType;
    private final String prefix;

    ContentTypePrefix(String contentType, String prefix) {
        this.contentType = contentType;
        this.prefix = prefix;
    }

    public static String getPrefix(String contentType) {
        if (contentType == null) {
            return "misc_";
        }

        return Arrays.stream(values())
                .filter(type -> contentType.startsWith(type.contentType))
                .map(type -> type.prefix)
                .findFirst()
                .orElse("misc_");
    }
}