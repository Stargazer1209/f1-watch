import os
import sys
import stat
import shutil
import zipfile
import tarfile
import urllib.request
import subprocess

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
TOOLS_DIR = os.path.join(BASE_DIR, ".tools")
JDK_DIR = os.path.join(TOOLS_DIR, "jdk")
SDK_DIR = os.path.join(TOOLS_DIR, "android-sdk")
CMDLINE_DIR = os.path.join(SDK_DIR, "cmdline-tools", "latest")
LICENSES_DIR = os.path.join(SDK_DIR, "licenses")

IS_LINUX = sys.platform.startswith("linux")
IS_WINDOWS = sys.platform.startswith("win32")

PROXIES = {
    "http": "http://127.0.0.1:7890",
    "https": "http://127.0.0.1:7890"
}

def is_proxy_available():
    try:
        import socket
        with socket.create_connection(("127.0.0.1", 7890), timeout=1):
            return True
    except Exception:
        return False

USE_PROXY = is_proxy_available()
print(f"[*] Proxy status (127.0.0.1:7890): {'ACTIVE' if USE_PROXY else 'INACTIVE / DIRECT'}")

def get_opener():
    if USE_PROXY:
        proxy_handler = urllib.request.ProxyHandler(PROXIES)
        return urllib.request.build_opener(proxy_handler)
    return urllib.request.build_opener()

def download_file(url, target_path, name):
    print(f"[*] Downloading {name} from {url}...")
    opener = get_opener()
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (X11; Linux x86_64)"})
    with opener.open(req, timeout=180) as response:
        total_length = response.headers.get("content-length")
        total_size = int(total_length) if total_length else 0
        downloaded = 0
        with open(target_path, "wb") as f:
            while True:
                chunk = response.read(1024 * 1024)
                if not chunk:
                    break
                f.write(chunk)
                downloaded += len(chunk)
                if total_size > 0:
                    pct = int(downloaded / total_size * 100)
                    sys.stdout.write(f"\r    [{name}] {downloaded // (1024*1024)}MB / {total_size // (1024*1024)}MB ({pct}%)")
                else:
                    sys.stdout.write(f"\r    [{name}] {downloaded // (1024*1024)}MB")
                sys.stdout.flush()
    print(f"\n[✓] Downloaded {name}")

def extract_archive(archive_path, extract_to_dir, name):
    print(f"[*] Extracting {name} to {extract_to_dir}...")
    extract_temp = os.path.join(TOOLS_DIR, f"{name}_temp")
    if os.path.exists(extract_temp):
        shutil.rmtree(extract_temp, ignore_errors=True)
    os.makedirs(extract_temp, exist_ok=True)

    if archive_path.endswith(".tar.gz") or archive_path.endswith(".tgz"):
        with tarfile.open(archive_path, "r:gz") as tar_ref:
            tar_ref.extractall(extract_temp)
    elif archive_path.endswith(".zip"):
        with zipfile.ZipFile(archive_path, "r") as zip_ref:
            zip_ref.extractall(extract_temp)
    else:
        raise ValueError(f"Unsupported archive format: {archive_path}")

    # Detect single top-level folder
    items = [os.path.join(extract_temp, item) for item in os.listdir(extract_temp)]
    if len(items) == 1 and os.path.isdir(items[0]):
        inner_dir = items[0]
    else:
        inner_dir = extract_temp

    if os.path.exists(extract_to_dir):
        shutil.rmtree(extract_to_dir, ignore_errors=True)
    os.makedirs(os.path.dirname(extract_to_dir), exist_ok=True)
    shutil.move(inner_dir, extract_to_dir)
    shutil.rmtree(extract_temp, ignore_errors=True)

    if os.path.exists(archive_path):
        os.remove(archive_path)

    # Set executable permissions on Linux
    if IS_LINUX:
        for root, dirs, files in os.walk(extract_to_dir):
            for file in files:
                file_path = os.path.join(root, file)
                if "/bin/" in file_path or file_path.endswith((".sh", "java", "javac", "sdkmanager", "avdmanager")):
                    try:
                        st = os.stat(file_path)
                        os.chmod(file_path, st.st_mode | stat.S_IEXEC | stat.S_IXGRP | stat.S_IXOTH)
                    except Exception:
                        pass

    print(f"[✓] {name} successfully setup at {extract_to_dir}")

def setup_jdk():
    java_bin = os.path.join(JDK_DIR, "bin", "java.exe" if IS_WINDOWS else "java")
    if os.path.exists(java_bin):
        print(f"[✓] JDK 17 already exists at {JDK_DIR}")
        return

    os.makedirs(TOOLS_DIR, exist_ok=True)
    if IS_LINUX:
        jdk_url = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.14%2B7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.14_7.tar.gz"
        archive_path = os.path.join(TOOLS_DIR, "jdk17.tar.gz")
    else:
        jdk_url = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.20.1%2B1/OpenJDK17U-jdk_x64_windows_hotspot_17.0.20.1_1.zip"
        archive_path = os.path.join(TOOLS_DIR, "jdk17.zip")

    download_file(jdk_url, archive_path, "OpenJDK 17")
    extract_archive(archive_path, JDK_DIR, "jdk17")

def setup_cmdline_tools():
    sdkmanager_bin = os.path.join(CMDLINE_DIR, "bin", "sdkmanager.bat" if IS_WINDOWS else "sdkmanager")
    if os.path.exists(sdkmanager_bin):
        print(f"[✓] Android cmdline-tools already exists at {CMDLINE_DIR}")
        return

    os.makedirs(os.path.join(SDK_DIR, "cmdline-tools"), exist_ok=True)
    temp_zip = os.path.join(TOOLS_DIR, "cmdline-tools.zip")

    if IS_LINUX:
        url = "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
    else:
        url = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"

    download_file(url, temp_zip, "Android Command Line Tools")

    print("[*] Extracting cmdline-tools...")
    extract_temp = os.path.join(TOOLS_DIR, "cmdline_temp")
    if os.path.exists(extract_temp):
        shutil.rmtree(extract_temp, ignore_errors=True)
    os.makedirs(extract_temp, exist_ok=True)

    with zipfile.ZipFile(temp_zip, "r") as zip_ref:
        zip_ref.extractall(extract_temp)

    extracted_inner = os.path.join(extract_temp, "cmdline-tools")
    if os.path.exists(CMDLINE_DIR):
        shutil.rmtree(CMDLINE_DIR, ignore_errors=True)
    os.makedirs(os.path.dirname(CMDLINE_DIR), exist_ok=True)
    shutil.move(extracted_inner, CMDLINE_DIR)

    shutil.rmtree(extract_temp, ignore_errors=True)
    if os.path.exists(temp_zip):
        os.remove(temp_zip)

    if IS_LINUX:
        bin_dir = os.path.join(CMDLINE_DIR, "bin")
        if os.path.exists(bin_dir):
            for file in os.listdir(bin_dir):
                file_path = os.path.join(bin_dir, file)
                try:
                    st = os.stat(file_path)
                    os.chmod(file_path, st.st_mode | stat.S_IEXEC | stat.S_IXGRP | stat.S_IXOTH)
                except Exception:
                    pass

    print(f"[✓] Android command line tools setup at {CMDLINE_DIR}")

def write_licenses():
    os.makedirs(LICENSES_DIR, exist_ok=True)
    licenses = {
        "android-sdk-license": "\n24333f8a63b6825ea9c5514f83c2829b004d1fee\n84831b9409646a918e30573bab4c9c91346d8abd\nd56f5187479451eabf01fb78af6dfcb131a6481e\n859f317696f67ef3d7f30a50a5560e7834b43903\n",
        "android-sdk-preview-license": "\n84831b9409646a918e30573bab4c9c91346d8abd\n",
        "android-googletv-license": "\n601085b94cd77f0b54ff86406957099fed7926d0\n",
        "android-sdk-arm-dbt-license": "\n859f317696f67ef3d7f30a50a5560e7834b43903\n",
        "google-gdk-license": "\n2520f1cddf284ca9f75247817a26e522c1743773\n",
        "intel-android-extra-license": "\nd975f751698a77b662f1254ddbeed3901e976f5a\n",
        "mips-android-sysimage-license": "\ne9acab5b5fbb560a725114e6e440b64849e507cc\n"
    }
    for name, content in licenses.items():
        with open(os.path.join(LICENSES_DIR, name), "w", encoding="utf-8") as f:
            f.write(content)
    print("[✓] Accepted Android SDK licenses written.")

def install_android_packages():
    sdkmanager = os.path.join(CMDLINE_DIR, "bin", "sdkmanager.bat" if IS_WINDOWS else "sdkmanager")
    if not os.path.exists(sdkmanager):
        raise FileNotFoundError(f"sdkmanager not found at {sdkmanager}")

    platform_dir = os.path.join(SDK_DIR, "platforms", "android-34")
    build_tools_dir = os.path.join(SDK_DIR, "build-tools", "34.0.0")
    if os.path.exists(platform_dir) and os.path.exists(build_tools_dir):
        print(f"[✓] Android SDK platforms;android-34 and build-tools;34.0.0 already installed.")
        return

    env = os.environ.copy()
    env["JAVA_HOME"] = JDK_DIR
    env["ANDROID_HOME"] = SDK_DIR
    env["ANDROID_SDK_ROOT"] = SDK_DIR
    env["PATH"] = f"{os.path.join(JDK_DIR, 'bin')}{os.pathsep}{env.get('PATH', '')}"

    cmd = [
        sdkmanager,
        f"--sdk_root={SDK_DIR}",
        "platforms;android-34",
        "build-tools;34.0.0",
        "platform-tools"
    ]
    if USE_PROXY:
        cmd.extend([
            "--proxy=http",
            "--proxy_host=127.0.0.1",
            "--proxy_port=7890"
        ])

    print(f"[*] Installing Android SDK packages via sdkmanager...")
    process = subprocess.Popen(cmd, env=env, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    for line in process.stdout:
        print("    " + line, end="")
    process.wait()
    if process.returncode != 0:
        print(f"[!] Warning: sdkmanager exited with code {process.returncode}")
    else:
        print("[✓] Android SDK packages successfully installed.")

def setup_project_configs():
    # 1. local.properties
    local_props_path = os.path.join(BASE_DIR, "local.properties")
    sdk_dir_escaped = SDK_DIR.replace("\\", "\\\\") if IS_WINDOWS else SDK_DIR
    with open(local_props_path, "w", encoding="utf-8") as f:
        f.write(f"sdk.dir={sdk_dir_escaped}\n")
    print(f"[✓] Generated local.properties with sdk.dir={SDK_DIR}")

    # 2. gradle.properties (update org.gradle.java.home dynamically)
    gradle_props_path = os.path.join(BASE_DIR, "gradle.properties")
    jdk_dir_escaped = JDK_DIR.replace("\\", "\\\\") if IS_WINDOWS else JDK_DIR

    lines = [
        "org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8",
        f"org.gradle.java.home={jdk_dir_escaped}",
        "android.useAndroidX=true",
        "android.nonTransitiveRClass=true",
        ""
    ]
    if USE_PROXY:
        lines.extend([
            "# Local Proxy configuration for Gradle downloads",
            "systemProp.http.proxyHost=127.0.0.1",
            "systemProp.http.proxyPort=7890",
            "systemProp.https.proxyHost=127.0.0.1",
            "systemProp.https.proxyPort=7890",
            ""
        ])
    with open(gradle_props_path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"[✓] Updated gradle.properties with isolated JDK path.")

    # 3. chmod gradlew on Linux
    if IS_LINUX:
        gradlew_path = os.path.join(BASE_DIR, "gradlew")
        if os.path.exists(gradlew_path):
            st = os.stat(gradlew_path)
            os.chmod(gradlew_path, st.st_mode | stat.S_IEXEC | stat.S_IXGRP | stat.S_IXOTH)
            print("[✓] chmod +x gradlew applied.")

def main():
    print("=" * 60)
    print("  F1 Watch Isolated Environment Setup (Zero System Pollution)")
    print(f"  OS: {'Linux' if IS_LINUX else 'Windows' if IS_WINDOWS else sys.platform}")
    print(f"  Target Root: {TOOLS_DIR}")
    print("=" * 60)

    setup_jdk()
    setup_cmdline_tools()
    write_licenses()
    install_android_packages()
    setup_project_configs()

    print("\n" + "=" * 60)
    print("🎉 Isolated Environment Setup Complete!")
    print(f"  JDK 17 Home:    {JDK_DIR}")
    print(f"  Android SDK:    {SDK_DIR}")
    print("=" * 60)
    if IS_LINUX:
        print("\nTo build or run tests locally:")
        print(f"  export JAVA_HOME=\"{JDK_DIR}\"")
        print(f"  export ANDROID_HOME=\"{SDK_DIR}\"")
        print("  ./gradlew testDebugUnitTest")
    else:
        print("\nTo build or run tests locally (PowerShell):")
        print(f'  $env:JAVA_HOME = "{JDK_DIR}"')
        print(f'  $env:ANDROID_HOME = "{SDK_DIR}"')
        print("  .\\gradlew.bat testDebugUnitTest")

if __name__ == "__main__":
    main()

