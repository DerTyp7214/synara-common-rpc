fn main() {
    let target_os = std::env::var("CARGO_CFG_TARGET_OS").unwrap();
    let target_arch = std::env::var("CARGO_CFG_TARGET_ARCH").unwrap();

    let kotlin_target = match target_os.as_str() {
        "linux" => "linux",
        "windows" => "windows",
        "macos" => match target_arch.as_str() {
            "x86_64" => "macosx64",
            "aarch64" => "macosarm",
            _ => panic!("Unsupported macOS architecture: {}", target_arch),
        },
        _ => panic!("Unsupported OS: {}", target_os),
    };

    let profile = std::env::var("PROFILE").unwrap_or("debug".to_string());
    let build_type = if profile == "release" { "releaseStatic" } else { "debugStatic" };

    let manifest_dir = std::env::var("CARGO_MANIFEST_DIR").unwrap();
    let base_path = std::path::Path::new(&manifest_dir)
        .join("..")
        .join("build")
        .join("bin")
        .join(kotlin_target)
        .join(build_type);

    let header_name = "libcommon_rpc_api.h";
    let default_header_path = base_path.join(header_name);

    let header_path = std::env::var("RPC_HEADER_PATH")
        .map(std::path::PathBuf::from)
        .unwrap_or(default_header_path);

    if !header_path.exists() {
        let profile_capitalized = if profile == "release" { "Release" } else { "Debug" };
        let task_suffix = match kotlin_target {
            "linux" => "Linux",
            "windows" => "Windows",
            "macosx64" => "Macosx64",
            "macosarm" => "Macosarm",
            _ => "Unknown",
        };

        panic!(
            "Header file not found at: {}\nPlease run './gradlew link{}Static{}' to generate it.",
            header_path.display(),
            profile_capitalized,
            task_suffix
        );
    }

    println!("cargo:rustc-link-search={}", base_path.display());
    println!("cargo:rustc-link-lib=static=common_rpc");
    println!("cargo:rerun-if-changed=build.rs");
    println!("cargo:rerun-if-changed={}", header_path.display());

    let bindings = bindgen::Builder::default()
        .header(header_path.to_str().expect("Invalid header path"))
        .parse_callbacks(Box::new(bindgen::CargoCallbacks::new()))
        .generate()
        .expect("Unable to generate bindings");

    let out_path = std::path::PathBuf::from(std::env::var("OUT_DIR").unwrap());
    bindings
        .write_to_file(out_path.join("bindings.rs"))
        .expect("Couldn't write bindings!");
}
