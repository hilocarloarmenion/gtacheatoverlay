//! Shared Rust source for the original Android GTA SA v2.10 cheat catalog.
//! Input injection is deliberately NOT implemented: Android overlays cannot
//! safely guarantee keyboard key events in another process without a tested bridge.
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;

const CATALOG: &str = include_str!("../../data/cheats.original-android-2.10.json");

#[no_mangle]
pub extern "system" fn Java_com_hilocarloarmenion_gtacheatoverlay_NativeCatalog_catalogJsonNative(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    match env.new_string(CATALOG) {
        Ok(output) => output.into_raw(),
        Err(_) => {
            let _ = env.throw_new("java/lang/IllegalStateException", "Cannot load Rust cheat catalog");
            std::ptr::null_mut()
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn catalog_contains_six_categories_and_known_android_code() {
        assert!(CATALOG.contains("\"CJ & Abilities\""));
        assert!(CATALOG.contains("\"Weapons & Gear\""));
        assert!(CATALOG.contains("\"Vehicles & Traffic\""));
        assert!(CATALOG.contains("\"People & Police\""));
        assert!(CATALOG.contains("\"Weather & Time\""));
        assert!(CATALOG.contains("\"World & Gameplay\""));
        assert!(CATALOG.contains("PJYNQCQ"));
    }
}
