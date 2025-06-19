// Build script for circe-rust-wrapper
use std::env;
use std::path::Path;

fn main() {
    let manifest_dir = env::var("CARGO_MANIFEST_DIR").unwrap();
    let target_dir = format!("{}/target", manifest_dir);
    
    // Look for native library
    let native_lib_shared = format!("{}/libcirce-native.so", target_dir);
    let native_lib_static = format!("{}/libcirce-native.a", target_dir);
    
    // Check for static library first (true static linking)
    if Path::new(&native_lib_static).exists() {
        println!("cargo:warning=Using static GraalVM library: {}", native_lib_static);
        
        // Configure static linking
        println!("cargo:rustc-link-search=native={}", target_dir);
        println!("cargo:rustc-link-lib=static=circe-native");
        
        // Add required system libraries for static linking
        println!("cargo:rustc-link-lib=dylib=dl");
        println!("cargo:rustc-link-lib=dylib=pthread");
        println!("cargo:rustc-link-lib=dylib=m");
        
        println!("cargo:rerun-if-changed={}", native_lib_static);
        
    } else if Path::new(&native_lib_shared).exists() {
        println!("cargo:warning=Using shared GraalVM library: {}", native_lib_shared);
        
        // Configure dynamic linking (original approach)
        println!("cargo:rustc-link-search=native={}", target_dir);
        println!("cargo:rustc-link-lib=dylib=circe-native");
        
        // Add rpath for runtime library loading
        println!("cargo:rustc-link-arg=-Wl,-rpath,{}", target_dir);
        
        // For development builds, also set the library path
        if let Ok(profile) = env::var("PROFILE") {
            if profile == "debug" {
                println!("cargo:rustc-env=LD_LIBRARY_PATH={}", target_dir);
            }
        }
        
        println!("cargo:rerun-if-changed={}", native_lib_shared);
        
    } else {
        println!("cargo:warning=GraalVM native library not found at: {} or {}", native_lib_shared, native_lib_static);
        println!("cargo:warning=To build the native library:");
        println!("cargo:warning=1. Run: mvn package -DskipTests");
        println!("cargo:warning=2. For shared library: ./build-graalvm.sh");
        println!("cargo:warning=3. For static library: ./build-graalvm.sh static");
        println!("cargo:warning=4. Then rebuild this crate");
    }
}
