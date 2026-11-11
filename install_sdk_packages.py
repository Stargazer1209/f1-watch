import os
import sys
import subprocess

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
TOOLS_DIR = os.path.join(BASE_DIR, ".tools")
SDK_DIR = os.path.join(TOOLS_DIR, "android-sdk")
LICENSES_DIR = os.path.join(SDK_DIR, "licenses")
CMDLINE_DIR = os.path.join(SDK_DIR, "cmdline-tools", "latest")
JDK_DIR = os.path.join(TOOLS_DIR, "jdk")

IS_LINUX = sys.platform.startswith("linux")
IS_WINDOWS = sys.platform.startswith("win32")
SDKMANAGER = os.path.join(CMDLINE_DIR, "bin", "sdkmanager.bat" if IS_WINDOWS else "sdkmanager")

def is_proxy_available():
    try:
        import socket
        with socket.create_connection(("127.0.0.1", 7890), timeout=1):
            return True
    except Exception:
        return False

USE_PROXY = is_proxy_available()

os.makedirs(LICENSES_DIR, exist_ok=True)

# Write accepted licenses
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

print("[✓] Licenses written successfully.")

env = os.environ.copy()
env["JAVA_HOME"] = JDK_DIR
env["ANDROID_HOME"] = SDK_DIR
env["ANDROID_SDK_ROOT"] = SDK_DIR
env["PATH"] = f"{os.path.join(JDK_DIR, 'bin')}{os.pathsep}{env.get('PATH', '')}"

# Install platforms;android-34 build-tools;34.0.0 platform-tools
cmd = [
    SDKMANAGER,
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

print("Installing Android SDK packages...")
process = subprocess.Popen(cmd, env=env, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
for line in process.stdout:
    print(line, end="")
process.wait()
print(f"Installation finished with returncode {process.returncode}")

