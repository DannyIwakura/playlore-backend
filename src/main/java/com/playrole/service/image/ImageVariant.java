package com.playrole.service.image;

public enum ImageVariant {
    USER_AVATAR("avatars", 200, 200, 200 * 1024, true, false),
    CHARACTER_AVATAR("personajes", 400, 400, 300 * 1024, true, false),
    CHANNEL_IMAGE("canales", 400, 400, 300 * 1024, true, false),
    CHARACTER_GALLERY("galeria", 1920, 1920, 1024 * 1024, false, true);

    private final String folder;
    private final int maxWidth;
    private final int maxHeight;
    private final int maxBytes;
    private final boolean square;
    private final boolean allowGif;

    ImageVariant(String folder, int maxWidth, int maxHeight, int maxBytes, boolean square, boolean allowGif) {
        this.folder = folder;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.maxBytes = maxBytes;
        this.square = square;
        this.allowGif = allowGif;
    }

    public String getFolder() {
        return folder;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getMaxBytes() {
        return maxBytes;
    }

    public boolean isSquare() {
        return square;
    }

    public boolean isAllowGif() {
        return allowGif;
    }
}
