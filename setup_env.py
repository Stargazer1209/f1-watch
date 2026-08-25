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
JDK_DIR = os.path.join(TOOLS_DIR, "jdk")
GRADLE_DIR = os.path.join(TOOLS_DIR, "gradle")
SDK_DIR = os.path.join(TOOLS_DIR, "android-sdk")

os.makedirs(TOOLS_DIR, exist_ok=True)
os.makedirs(SDK_DIR, exist_ok=True)

def download_and_extract(url, target_dir, name):
    if os.path.exists(target_dir) and os.listdir(target_dir):
        print(f"[{name}] already exists at {target_dir}, skipping download.")
        return

    temp_zip = os.path.join(TOOLS_DIR, f"{name}.zip")
    print(f"Downloading {name} from {url}...")
    
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
                        sys.stdout.write(f"\r[{name}] {downloaded // (1024*1024)}MB / {int(total_length) // (1024*1024)}MB ({pct}%)")
                    else:
                        sys.stdout.write(f"\r[{name}] {downloaded // (1024*1024)}MB")
                    sys.stdout.flush()
    print(f"\nExtracting {name} to {target_dir}...")
    extract_temp = os.path.join(TOOLS_DIR, f"{name}_temp")
    os.makedirs(extract_temp, exist_ok=True)
    with zipfile.ZipFile(temp_zip, 'r') as zip_ref:
        zip_ref.extractall(extract_temp)

    # find root folder in extract_temp
    items = os.listdir(extract_temp)
    if len(items) == 1 and os.path.isdir(os.path.join(extract_temp, items[0])):
        inner_dir = os.path.join(extract_temp, items[0])
        if os.path.exists(target_dir):
            os.rmdir(target_dir)
        os.rename(inner_dir, target_dir)
        os.rmdir(extract_temp)
    else:
        if os.path.exists(target_dir):
            os.rmdir(target_dir)
        os.rename(extract_temp, target_dir)
    
    if os.path.exists(temp_zip):
        os.remove(temp_zip)
    print(f"[{name}] successfully setup at {target_dir}")

def main():
    # 1. Download OpenJDK 17
    jdk_url = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.20.1%2B1/OpenJDK17U-jdk_x64_windows_hotspot_17.0.20.1_1.zip"
    download_and_extract(jdk_url, JDK_DIR, "jdk17")

    # 2. Download Gradle 8.9
    gradle_url = "https://services.gradle.org/distributions/gradle-8.9-bin.zip"
    download_and_extract(gradle_url, GRADLE_DIR, "gradle89")

    print("\nEnvironment setup complete!")
    print(f"JDK Path: {JDK_DIR}")
    print(f"Gradle Path: {GRADLE_DIR}")
    print(f"Android SDK Path: {SDK_DIR}")

if __name__ == '__main__':
    main()
