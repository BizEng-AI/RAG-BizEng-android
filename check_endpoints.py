import requests

"""
Quick script to check which endpoints are available on the server
"""

BASE_URL = "https://bizeng-server.fly.dev"

print("🔍 Checking available endpoints...\n")

endpoints = [
    ("GET", "/health"),
    ("GET", "/"),
    ("POST", "/auth/register"),
    ("POST", "/auth/login"),
    ("POST", "/auth/refresh"),
    ("GET", "/me"),
    ("POST", "/chat"),
    ("POST", "/ask"),
    ("POST", "/roleplay/start"),
    ("POST", "/pronunciation/assess"),
]

for method, endpoint in endpoints:
    try:
        if method == "GET":
            response = requests.get(f"{BASE_URL}{endpoint}", timeout=5)
        else:
            response = requests.post(f"{BASE_URL}{endpoint}", json={}, timeout=5)

        status = response.status_code
        if status == 404:
            print(f"❌ {method:6} {endpoint:30} → 404 Not Found")
        elif status == 422:
            print(f"⚠️  {method:6} {endpoint:30} → 422 (Validation Error - endpoint exists!)")
        elif status == 401:
            print(f"🔐 {method:6} {endpoint:30} → 401 (Needs auth - endpoint exists!)")
        elif status == 200:
            print(f"✅ {method:6} {endpoint:30} → 200 OK")
        else:
            print(f"ℹ️  {method:6} {endpoint:30} → {status}")
    except Exception as e:
        print(f"❌ {method:6} {endpoint:30} → Error: {e}")

print("\n" + "="*60)
print("Legend:")
print("  ✅ = Endpoint works")
print("  ⚠️  = Endpoint exists but needs valid data")
print("  🔐 = Endpoint exists but needs authentication")
print("  ❌ = Endpoint not found or error")
print("="*60)

