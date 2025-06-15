use std::env;
use std::fs;
use std::path::Path;

fn main() {
    let target_os = env::var("CARGO_CFG_TARGET_OS").unwrap_or_else(|_| "unknown".to_string());
    let target_arch = env::var("CARGO_CFG_TARGET_ARCH").unwrap_or_else(|_| "unknown".to_string());
    
    // Determine the platform-specific directory
    let platform_dir = match (target_os.as_str(), target_arch.as_str()) {
        ("linux", "x86_64") => "linux-x86_64",
        ("macos", "x86_64") => "macos-x86_64",
        ("macos", "aarch64") => "macos-aarch64",
        ("windows", "x86_64") => "windows-x86_64",
        _ => {
            println!("cargo:warning=Unsupported platform {}-{}, using linux-x86_64", target_os, target_arch);
            "linux-x86_64"
        }
    };
    
    let out_dir = env::var("OUT_DIR").unwrap();
    let manifest_dir = env::var("CARGO_MANIFEST_DIR").unwrap();
    
    let native_binary_name = if target_os == "windows" { "circe-cli-native.exe" } else { "circe-cli-native" };
    let source_path = Path::new(&manifest_dir).join("native-binaries").join(platform_dir).join(native_binary_name);
    let dest_path = Path::new(&out_dir).join(native_binary_name);
    
    // Copy the native binary to the output directory
    if source_path.exists() {
        if let Err(e) = fs::copy(&source_path, &dest_path) {
            println!("cargo:warning=Failed to copy native binary: {}", e);
        } else {
            println!("cargo:rerun-if-changed={}", source_path.display());
            println!("Native binary copied to: {}", dest_path.display());
        }
    } else {
        println!("cargo:warning=Native binary not found at: {}", source_path.display());
        println!("cargo:warning=The library will attempt to find circe-cli-native in the system PATH or src/ directory");
    }
}