import os
import sys
import stat
import shutil
import zipfile
import urllib.request

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
TOOLS_DIR = os.path.join(BASE_DIR, ".tools")
SDK_DIR = os.path.join(TOOLS_DIR, "android-sdk")
CMDLINE_DIR = os.path.join(SDK_DIR, "cmdline-tools", "latest")

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

def setup_cmdline_tools():
    sdkmanager_bin = os.path.join(CMDLINE_DIR, "bin", "sdkmanager.bat" if IS_WINDOWS else "sdkmanager")
    if os.path.exists(sdkmanager_bin):
        print(f"[✓] sdkmanager already exists at {CMDLINE_DIR}")
        return

    os.makedirs(os.path.join(SDK_DIR, "cmdline-tools"), exist_ok=True)
    temp_zip = os.path.join(TOOLS_DIR, "cmdline-tools.zip")

    if IS_LINUX:
        url = "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
    else:
        url = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"

    print(f"Downloading Android Command Line Tools from {url}...")
    opener = urllib.request.build_opener(urllib.request.ProxyHandler(PROXIES) if USE_PROXY else urllib.request.ProxyHandler())
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (X11; Linux x86_64)"})

    with opener.open(req, timeout=120) as r:
        total_length = r.headers.get("content-length")
        total_size = int(total_length) if total_length else 0
        downloaded = 0
        with open(temp_zip, "wb") as f:
            while True:
                chunk = r.read(1024 * 1024)
                if not chunk:
                    break
                f.write(chunk)
                downloaded += len(chunk)
                if total_size:
                    pct = int(downloaded / total_size * 100)
                    sys.stdout.write(f"\r[cmdline-tools] {downloaded // (1024*1024)}MB / {total_size // (1024*1024)}MB ({pct}%)")
                else:
                    sys.stdout.write(f"\r[cmdline-tools] {downloaded // (1024*1024)}MB")
                sys.stdout.flush()

    print("\nExtracting cmdline-tools...")
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

    print(f"Android command line tools setup at {CMDLINE_DIR}")

if __name__ == "__main__":
    setup_cmdline_tools()

