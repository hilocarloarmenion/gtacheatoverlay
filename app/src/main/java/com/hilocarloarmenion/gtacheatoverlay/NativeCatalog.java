package com.hilocarloarmenion.gtacheatoverlay;

final class NativeCatalog {
    private static final boolean loaded;
    static {
        boolean ok;
        try { System.loadLibrary("gtacheatcore"); ok = true; }
        catch (UnsatisfiedLinkError e) { ok = false; }
        loaded = ok;
    }
    private static native String catalogJsonNative();
    private static native boolean isKnownCheatNative(String code);
    static String read() {
        if (!loaded) throw new IllegalStateException("Rust core missing for this ABI");
        return catalogJsonNative();
    }
    static boolean isKnownCheat(String code) {
        return loaded && code != null && isKnownCheatNative(code);
    }
    private NativeCatalog() {}
}
