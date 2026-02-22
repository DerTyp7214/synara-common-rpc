fn main() {
    let header_path = std::env::var("RPC_HEADER_PATH").unwrap_or("../build/bin/native/debugShared/common-rpc.h".to_string());
    let bindings = bindgen::Builder::default()
        .header(header_path)
        .parse_callbacks(Box::new(bindgen::CargoCallbacks::new()))
        .generate()
        .expect("Unable to generate bindings");

    let out_path = std::path::PathBuf::from(std::env::var("OUT_DIR").unwrap());
    bindings
        .write_to_file(out_path.join("bindings.rs"))
        .expect("Couldn't write bindings!");
}
