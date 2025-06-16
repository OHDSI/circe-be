use std::env;
use std::fs;
use std::path::{Path, PathBuf};
use std::process::Command;

fn main() {
    // Configure linking to GraalVM native library
    println!("cargo:rustc-link-search=native=target");
    println!("cargo:rustc-link-lib=dylib=circe-native");
    
    // Add rpath for runtime library loading
    let manifest_dir = env::var("CARGO_MANIFEST_DIR").unwrap();
    let target_dir = format!("{}/target", manifest_dir);
    println!("cargo:rustc-link-arg=-Wl,-rpath,{}", target_dir);
    
    // For development builds, also set the library path
    if let Ok(profile) = env::var("PROFILE") {
        if profile == "debug" {
            println!("cargo:rustc-env=LD_LIBRARY_PATH={}", target_dir);
        }
    }
    
    // Check if the native library exists
    if !std::path::Path::new("target/libcirce-native.so").exists() {
        println!("cargo:warning=GraalVM native library not found. Please run: ./build-graalvm.sh");
    }
}

/// Get the cache directory for downloaded binaries
fn get_cache_dir() -> PathBuf {
    let cache_base = if let Ok(cache_dir) = env::var("CARGO_HOME") {
        PathBuf::from(cache_dir)
    } else if let Ok(home_dir) = env::var("HOME") {
        PathBuf::from(home_dir).join(".cargo")
    } else {
        // Fallback to current directory
        env::current_dir().unwrap_or_else(|_| PathBuf::from("."))
    };
    
    cache_base.join("circe-native-cache")
}

/// Download native binary from GitHub repository
fn download_native_binary(platform_dir: &str, native_binary_name: &str, dest_path: &Path) -> Result<(), Box<dyn std::error::Error>> {
    // Try to get cached binary first
    let cache_dir = get_cache_dir();
    let cached_binary = cache_dir.join(platform_dir).join(native_binary_name);
    
    if cached_binary.exists() {
        println!("cargo:warning=Using cached native binary from: {}", cached_binary.display());
        if let Err(e) = fs::copy(&cached_binary, dest_path) {
            println!("cargo:warning=Failed to copy cached binary: {}", e);
        } else {
            return Ok(());
        }
    }
    
    // GitHub repository information
    let repo_owner = "p-hoffmann";
    let repo_name = "circe-be2";
    
    // Try multiple branches/sources
    let sources = vec![
        // Try current branch if available
        env::var("GITHUB_REF_NAME").unwrap_or_else(|_| "main".to_string()),
        "main".to_string(),
        "develop".to_string(),
        "master".to_string(),
    ];
    
    for branch in sources {
        let url = format!(
            "https://raw.githubusercontent.com/{}/{}/{}/native-binaries/{}/{}",
            repo_owner, repo_name, branch, platform_dir, native_binary_name
        );
        
        println!("cargo:warning=Attempting to download from branch '{}': {}", branch, url);
        
        if try_download_with_tools(&url, dest_path).is_ok() {
            // Cache the downloaded binary for future builds
            if let Err(e) = cache_downloaded_binary(dest_path, &cached_binary) {
                println!("cargo:warning=Failed to cache binary: {}", e);
            }
            return Ok(());
        }
    }
    
    Err("Failed to download native binary from any available source".into())
}

/// Cache the downloaded binary for future use
fn cache_downloaded_binary(source: &Path, cached_path: &Path) -> Result<(), Box<dyn std::error::Error>> {
    if let Some(parent) = cached_path.parent() {
        fs::create_dir_all(parent)?;
    }
    fs::copy(source, cached_path)?;
    
    println!("cargo:warning=Cached native binary for future builds: {}", cached_path.display());
    Ok(())
}

/// Try to download a file using available tools
fn try_download_with_tools(url: &str, dest_path: &Path) -> Result<(), Box<dyn std::error::Error>> {
    // Use curl to download the file (most systems have curl available)
    let output = Command::new("curl")
        .args(&["-L", "-s", "-f", "-o", dest_path.to_str().unwrap(), url])
        .output()?;
    
    if output.status.success() {
        // Verify the downloaded file exists and has reasonable size
        if dest_path.exists() {
            let metadata = fs::metadata(dest_path)?;
            if metadata.len() > 1000 { // Must be at least 1KB 
                // Make the binary executable on Unix systems
                #[cfg(unix)]
                {
                    use std::os::unix::fs::PermissionsExt;
                    let mut perms = metadata.permissions();
                    perms.set_mode(0o755);
                    fs::set_permissions(dest_path, perms)?;
                }
                
                println!("cargo:warning=Successfully downloaded native binary ({} bytes)", metadata.len());
                return Ok(());
            } else {
                // Remove small file (likely an error page)
                let _ = fs::remove_file(dest_path);
                return Err("Downloaded file is too small (likely an error)".into());
            }
        }
    }
    
    // Try wget as a fallback
    let output = Command::new("wget")
        .args(&["-q", "-O", dest_path.to_str().unwrap(), url])
        .output();
    
    if let Ok(output) = output {
        if output.status.success() && dest_path.exists() {
            let metadata = fs::metadata(dest_path)?;
            if metadata.len() > 1000 {
                #[cfg(unix)]
                {
                    use std::os::unix::fs::PermissionsExt;
                    let mut perms = metadata.permissions();
                    perms.set_mode(0o755);
                    fs::set_permissions(dest_path, perms)?;
                }
                
                println!("cargo:warning=Successfully downloaded native binary with wget ({} bytes)", metadata.len());
                return Ok(());
            } else {
                let _ = fs::remove_file(dest_path);
            }
        }
    }
    
    Err("Failed to download native binary using curl or wget".into())
}

/// Download or build native binary for the current platform
fn download_or_build_native_binary() -> Result<(), Box<dyn std::error::Error>> {
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
            println!("cargo:warning=Native binary copied successfully to: {}", dest_path.display());
        }
    } else {
        println!("cargo:warning=Native binary not found at: {}", source_path.display());
        println!("cargo:warning=Attempting to download from GitHub repository...");
        
        // Try to download the native binary from GitHub
        match download_native_binary(platform_dir, native_binary_name, &dest_path) {
            Ok(()) => {
                println!("cargo:warning=Native binary downloaded successfully to: {}", dest_path.display());
            },
            Err(e) => {
                println!("cargo:warning=Failed to download native binary: {}", e);
                println!("cargo:warning=The library will attempt to find circe-cli-native in the system PATH or src/ directory");
            }
        }
    }
    
    Ok(())
}