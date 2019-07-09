package net.sourceforge.peers.G729.spi;

public enum MediaType {
    AUDIO(0, "audio", 1),
    VIDEO(1, "video", 2);

    private int code;
    private String name;
    private int mask;

    private MediaType(int code, String name, int mask) {
        this.code = code;
        this.name = name;
        this.mask = mask;
    }

    public static MediaType getInstance(String name) {
        if (name.equalsIgnoreCase("audio")) {
            return AUDIO;
        } else if (name.equalsIgnoreCase("video")) {
            return VIDEO;
        } else {
            throw new IllegalArgumentException("There is no media type for: " + name);
        }
    }

    public static MediaType getMediaType(int code) {
        return code == 0 ? AUDIO : VIDEO;
    }

    public int getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public int getMask() {
        return this.mask;
    }
}