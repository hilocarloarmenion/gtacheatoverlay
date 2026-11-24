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
    static String read() {
        if (!loaded) throw new IllegalStateException("Rust core missing for this ABI");
        return catalogJsonNative();
    }
    private NativeCatalog() {}
}
