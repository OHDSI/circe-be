// Build script for circe-rust-wrapper
// This version uses a prebuilt native library checked into the repository
use std::env;
use std::path::Path;

fn main() {
    let manifest_dir = env::var("CARGO_MANIFEST_DIR").unwrap();
    
    // Check for the prebuilt native library first
    let native_libs_dir = format!("{}/native-libs/linux-x86_64", manifest_dir);
    let prebuilt_shared = format!("{}/libcirce-native.so", native_libs_dir);
    let prebuilt_static = format!("{}/libcirce-native.a", native_libs_dir);
    
    // Check for target directory builds (for development)
    let target_dir = format!("{}/target", manifest_dir);
    let target_shared = format!("{}/libcirce-native.so", target_dir);
    let target_static = format!("{}/libcirce-native.a", target_dir);
    
    // Priority order: target static > target shared > prebuilt static > prebuilt shared
    if Path::new(&target_static).exists() {
        println!("cargo:warning=Using development static GraalVM library: {}", target_static);
        configure_static_linking(&target_dir);
        println!("cargo:rerun-if-changed={}", target_static);
        
    } else if Path::new(&target_shared).exists() {
        println!("cargo:warning=Using development shared GraalVM library: {}", target_shared);
        configure_shared_linking(&target_dir);
        println!("cargo:rerun-if-changed={}", target_shared);
        
    } else if Path::new(&prebuilt_static).exists() {
        println!("cargo:warning=Using prebuilt static GraalVM library: {}", prebuilt_static);
        configure_static_linking(&native_libs_dir);
        println!("cargo:rerun-if-changed={}", prebuilt_static);
        
    } else if Path::new(&prebuilt_shared).exists() {
        println!("cargo:warning=Using prebuilt shared GraalVM library: {}", prebuilt_shared);
        configure_shared_linking(&native_libs_dir);
        println!("cargo:rerun-if-changed={}", prebuilt_shared);
        
    } else {
        println!("cargo:warning=GraalVM native library not found!");
        println!("cargo:warning=Searched for:");
        println!("cargo:warning=  - {}", target_static);
        println!("cargo:warning=  - {}", target_shared);
        println!("cargo:warning=  - {}", prebuilt_static);
        println!("cargo:warning=  - {}", prebuilt_shared);
        println!("cargo:warning=");
        println!("cargo:warning=To build the native library:");
        println!("cargo:warning=1. Run: mvn package -DskipTests");
        println!("cargo:warning=2. For shared library: ./build-graalvm.sh");
        println!("cargo:warning=3. For static library: ./build-graalvm.sh static");
        println!("cargo:warning=4. Then rebuild this crate");
    }
}

fn configure_static_linking(lib_dir: &str) {
    // Configure static linking
    println!("cargo:rustc-link-search=native={}", lib_dir);
    println!("cargo:rustc-link-lib=static=circe-native");
    
    // Add required system libraries for static linking
    println!("cargo:rustc-link-lib=dylib=dl");
    println!("cargo:rustc-link-lib=dylib=pthread");
    println!("cargo:rustc-link-lib=dylib=m");
    println!("cargo:rustc-link-lib=dylib=rt");
    println!("cargo:rustc-link-lib=dylib=z");
}

fn configure_shared_linking(lib_dir: &str) {
    // Configure dynamic linking
    println!("cargo:rustc-link-search=native={}", lib_dir);
    println!("cargo:rustc-link-lib=dylib=circe-native");
    
    // Add rpath for runtime library loading
    println!("cargo:rustc-link-arg=-Wl,-rpath,{}", lib_dir);
    
    // For development builds, also set the library path
    if let Ok(profile) = env::var("PROFILE") {
        if profile == "debug" {
            println!("cargo:rustc-env=LD_LIBRARY_PATH={}", lib_dir);
        }
    }
}
