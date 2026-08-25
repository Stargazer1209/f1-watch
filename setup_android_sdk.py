import os
import sys
import zipfile
import requests

PROXIES = {
    'http': 'http://127.0.0.1:7890',
    'https': 'http://127.0.0.1:7890'
}

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
TOOLS_DIR = os.path.join(BASE_DIR, ".tools")
SDK_DIR = os.path.join(TOOLS_DIR, "android-sdk")
CMDLINE_DIR = os.path.join(SDK_DIR, "cmdline-tools", "latest")

def setup_cmdline_tools():
    if os.path.exists(os.path.join(CMDLINE_DIR, "bin", "sdkmanager.bat")):
        print(f"sdkmanager already exists at {CMDLINE_DIR}")
        return

    os.makedirs(os.path.join(SDK_DIR, "cmdline-tools"), exist_ok=True)
    temp_zip = os.path.join(TOOLS_DIR, "cmdline-tools.zip")
    url = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
    print(f"Downloading Android Command Line Tools from {url}...")
    
    with requests.get(url, proxies=PROXIES, stream=True, timeout=120) as r:
        r.raise_for_status()
        total_length = r.headers.get('content-length')
        downloaded = 0
        with open(temp_zip, 'wb') as f:
            for chunk in r.iter_content(chunk_size=1024*1024):
                if chunk:
                    f.write(chunk)
                    downloaded += len(chunk)
                    if total_length:
                        pct = int(downloaded / int(total_length) * 100)
                        sys.stdout.write(f"\r[cmdline-tools] {downloaded // (1024*1024)}MB / {int(total_length) // (1024*1024)}MB ({pct}%)")
                    else:
                        sys.stdout.write(f"\r[cmdline-tools] {downloaded // (1024*1024)}MB")
                    sys.stdout.flush()
    
    print("\nExtracting cmdline-tools...")
    extract_temp = os.path.join(TOOLS_DIR, "cmdline_temp")
    os.makedirs(extract_temp, exist_ok=True)
    with zipfile.ZipFile(temp_zip, 'r') as zip_ref:
        zip_ref.extractall(extract_temp)

    # Move cmdline-tools inner content to CMDLINE_DIR
    extracted_inner = os.path.join(extract_temp, "cmdline-tools")
    if os.path.exists(CMDLINE_DIR):
        import shutil
        shutil.rmtree(CMDLINE_DIR)
    os.rename(extracted_inner, CMDLINE_DIR)
    
    import shutil
    shutil.rmtree(extract_temp, ignore_errors=True)
    if os.path.exists(temp_zip):
        os.remove(temp_zip)
    print(f"Android command line tools setup at {CMDLINE_DIR}")

if __name__ == '__main__':
    setup_cmdline_tools()
