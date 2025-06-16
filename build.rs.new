use std::env;
use std::path::Path;

fn main() {
    let manifest_dir = env::var("CARGO_MANIFEST_DIR").unwrap();
    let target_dir = format!("{}/target", manifest_dir);
    let native_lib_path = format!("{}/libcirce-native.so", target_dir);
    
    // Configure linking to GraalVM native library
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
    
    // Check if the native library exists and provide helpful error message
    if !Path::new(&native_lib_path).exists() {
        println!("cargo:warning=GraalVM native library not found at: {}", native_lib_path);
        println!("cargo:warning=To build the native library:");
        println!("cargo:warning=1. Run: mvn package -DskipTests");
        println!("cargo:warning=2. Run: ./build-graalvm.sh");
        println!("cargo:warning=3. Then rebuild this crate");
        // Continue with build - the linker will fail if library is truly missing
    } else {
        println!("cargo:warning=Found GraalVM native library at: {}", native_lib_path);
    }
}
