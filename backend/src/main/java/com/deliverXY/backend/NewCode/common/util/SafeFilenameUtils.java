package com.deliverXY.backend.NewCode.common.util;

import java.util.Set;

public final class SafeFilenameUtils {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".pdf");

    private SafeFilenameUtils() {
    }

    public static String safeExtension(String validatedFilename) {
        if (!validatedFilename.contains(".")) {
            return "";
        }
        String extension = validatedFilename.substring(validatedFilename.lastIndexOf('.')).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file extension");
        }
        return extension;
    }
}
