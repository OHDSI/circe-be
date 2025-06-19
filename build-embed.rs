// Build script for embedding the native library into Rust binary
use std::env;
use std::fs;
use std::path::Path;

fn main() {
    let manifest_dir = env::var("CARGO_MANIFEST_DIR").unwrap();
    let target_dir = format!("{}/target", manifest_dir);
    let out_dir = env::var("OUT_DIR").unwrap();
    
    // Look for native library
    let native_lib_shared = format!("{}/libcirce-native.so", target_dir);
    let embedded_lib_path = format!("{}/libcirce-native-embedded.a", out_dir);
    
    if Path::new(&native_lib_shared).exists() {
        println!("cargo:warning=Embedding GraalVM library for static-like linking");
        
        // Copy the shared library to the output directory as static
        fs::copy(&native_lib_shared, &embedded_lib_path)
            .expect("Failed to copy native library");
        
        // Configure linking to the embedded library
        println!("cargo:rustc-link-search=native={}", out_dir);
        println!("cargo:rustc-link-lib=static=circe-native-embedded");
        
        // Add required system libraries
        println!("cargo:rustc-link-lib=dylib=dl");
        println!("cargo:rustc-link-lib=dylib=pthread");
        println!("cargo:rustc-link-lib=dylib=m");
        
        // For better distribution, we can set static linking flags
        if env::var("CIRCE_FULLY_STATIC").is_ok() {
            println!("cargo:rustc-link-arg=-static-libgcc");
            println!("cargo:rustc-link-arg=-static-libstdc++");
        }
        
        println!("cargo:rerun-if-changed={}", native_lib_shared);
        
    } else {
        println!("cargo:warning=Native library not found at: {}", native_lib_shared);
        println!("cargo:warning=Run './build-graalvm.sh' first");
    }
}
