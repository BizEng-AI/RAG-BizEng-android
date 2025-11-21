import requests
import json
import sys

print("\n=== TESTING PREVIOUSLY FAILED ENDPOINTS ===\n")

# Login
print("Step 1: Getting admin token...")
try:
    login_response = requests.post(
        "https://bizeng-server.fly.dev/auth/login",
        json={"email": "yoo@gmail.com", "password": "qwerty"},
        timeout=10
    )
    login_response.raise_for_status()
    token = login_response.json()["access_token"]
    print(f"✓ Token obtained: {token[:20]}...")
except Exception as e:
    print(f"✗ Login failed: {e}")
    sys.exit(1)

headers = {"Authorization": f"Bearer {token}"}

# Test users_activity
print("\n" + "="*50)
print("TEST 1: /admin/monitor/users_activity")
print("="*50)
try:
    response = requests.get(
        "https://bizeng-server.fly.dev/admin/monitor/users_activity?days=30",
        headers=headers,
        timeout=10
    )
    print(f"Status Code: {response.status_code}")

    if response.status_code == 200:
        data = response.json()
        print(f"✅ SUCCESS - Endpoint is working!")
        print(f"Total student records: {len(data)}")
        if len(data) > 0:
            print(f"\nFirst record preview:")
            print(json.dumps(data[0], indent=2))
    elif response.status_code == 404:
        print(f"❌ FAILED - Endpoint not found (404)")
        print(f"Response: {response.text}")
    else:
        print(f"❌ FAILED - Status {response.status_code}")
        print(f"Response: {response.text}")
except Exception as e:
    print(f"❌ FAILED - Exception: {e}")

# Test groups_activity
print("\n" + "="*50)
print("TEST 2: /admin/monitor/groups_activity")
print("="*50)
try:
    response = requests.get(
        "https://bizeng-server.fly.dev/admin/monitor/groups_activity?days=30",
        headers=headers,
        timeout=10
    )
    print(f"Status Code: {response.status_code}")

    if response.status_code == 200:
        data = response.json()
        print(f"✅ SUCCESS - Endpoint is working!")
        print(f"Total group records: {len(data)}")
        if len(data) > 0:
            print(f"\nGroups preview:")
            for group in data:
                print(f"  - {group.get('group_name')}: {group.get('student_count')} students, {group.get('total_exercises')} exercises")
    elif response.status_code == 404:
        print(f"❌ FAILED - Endpoint not found (404)")
        print(f"Response: {response.text}")
    else:
        print(f"❌ FAILED - Status {response.status_code}")
        print(f"Response: {response.text}")
except Exception as e:
    print(f"❌ FAILED - Exception: {e}")

print("\n" + "="*50)
print("TESTS COMPLETE")
print("="*50)

