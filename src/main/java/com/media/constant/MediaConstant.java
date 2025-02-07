package com.media.constant;

import java.io.File;

public class MediaConstant {
    public static final String DIRECTORY_TENANT = File.separator + "tenant";
    public static final String DIRECTORY_GENERAL = File.separator + "general";

    public static final String CMD_DELETE_MEDIA_TENANT = "CMD_DELETE_MEDIA_TENANT";

    public static final Integer USER_KIND_ADMIN = 1;
    public static final Integer USER_KIND_CUSTOMER = 2;

    private MediaConstant() {
        throw new IllegalStateException("Utility class");
    }
}
