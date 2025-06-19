// Alternative build.rs for embedding static library
use std::env;
use std::path::Path;

fn main() {
    let manifest_dir = env::var("CARGO_MANIFEST_DIR").unwrap();
    let target_dir = format!("{}/target", manifest_dir);
    let native_lib_static = format!("{}/libcirce-native.a", target_dir);
    
    // Force static linking if static library exists
    if Path::new(&native_lib_static).exists() {
        println!("cargo:warning=Embedding static GraalVM library: {}", native_lib_static);
        
        // Link the static library directly
        println!("cargo:rustc-link-search=native={}", target_dir);
        println!("cargo:rustc-link-lib=static=circe-native");
        
        // Add required system libraries for GraalVM static builds
        println!("cargo:rustc-link-lib=dylib=dl");      // Dynamic loading
        println!("cargo:rustc-link-lib=dylib=pthread"); // Threading
        println!("cargo:rustc-link-lib=dylib=m");       // Math
        println!("cargo:rustc-link-lib=dylib=rt");      // Realtime (if needed)
        println!("cargo:rustc-link-lib=dylib=z");       // Compression (if needed)
        
        // For musl static builds, we might need additional flags
        if env::var("TARGET").unwrap_or_default().contains("musl") {
            println!("cargo:rustc-link-arg=-static");
            println!("cargo:rustc-link-lib=static=c");
        }
        
        // Rerun if the static library changes
        println!("cargo:rerun-if-changed={}", native_lib_static);
        
    } else {
        println!("cargo:warning=Static library not found at: {}", native_lib_static);
        println!("cargo:warning=Run './build-graalvm.sh static' to create it");
        
        // Fall back to dynamic linking or fail
        let native_lib_shared = format!("{}/libcirce-native.so", target_dir);
        if Path::new(&native_lib_shared).exists() {
            println!("cargo:warning=Falling back to dynamic linking");
            println!("cargo:rustc-link-search=native={}", target_dir);
            println!("cargo:rustc-link-lib=dylib=circe-native");
            println!("cargo:rustc-link-arg=-Wl,-rpath,{}", target_dir);
        }
    }
}
