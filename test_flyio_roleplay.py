"""Test if Fly.io server has roleplay endpoints"""
import sys
import json

try:
    import requests
except ImportError:
    print("ERROR: requests module not installed")
    print("Run: pip install requests")
    sys.exit(1)

BASE_URL = "https://bizeng-server.fly.dev"

print("=" * 60)
print("TESTING FLY.IO SERVER ENDPOINTS")
print("=" * 60)
print(f"Base URL: {BASE_URL}")
print()

# Test 1: Health check
print("1. Testing /health endpoint...")
try:
    r = requests.get(f"{BASE_URL}/health", timeout=15)
    print(f"   ✅ Status: {r.status_code}")
    print(f"   Response: {r.text}")
except requests.exceptions.Timeout:
    print(f"   ⏱️  TIMEOUT - Server is cold starting, try again in 30 sec")
except Exception as e:
    print(f"   ❌ ERROR: {e}")

# Test 2: RAG search (quick test)
print("\n2. Testing /debug/search endpoint...")
try:
    r = requests.get(f"{BASE_URL}/debug/search?q=meeting&k=3", timeout=30)
    print(f"   ✅ Status: {r.status_code}")
    if r.status_code == 200:
        data = r.json()
        print(f"   ✅ Found {len(data.get('items', []))} results")
    else:
        print(f"   Response: {r.text[:200]}")
except requests.exceptions.Timeout:
    print(f"   ⏱️  TIMEOUT - Server might be processing, try again")
except Exception as e:
    print(f"   ❌ ERROR: {e}")

# Test 3: List roleplay scenarios
print("\n3. Testing /roleplay/scenarios endpoint...")
try:
    r = requests.get(f"{BASE_URL}/roleplay/scenarios", timeout=30)
    print(f"   Status: {r.status_code}")
    if r.status_code == 200:
        data = r.json()
        print(f"   ✅ SUCCESS! Found {len(data)} scenarios")
        for scenario in data:
            print(f"      - {scenario.get('id')}: {scenario.get('title')}")
    elif r.status_code == 404:
        print(f"   ❌ ENDPOINT NOT FOUND (404)")
        print(f"   This means the roleplay module was not deployed to Fly.io")
    else:
        print(f"   Response: {r.text[:300]}")
except requests.exceptions.Timeout:
    print(f"   ⏱️  TIMEOUT")
except Exception as e:
    print(f"   ❌ ERROR: {e}")

# Test 4: Chat endpoint
print("\n4. Testing /chat endpoint...")
try:
    r = requests.post(
        f"{BASE_URL}/chat",
        json={"messages": [{"role": "user", "content": "Hello"}]},
        timeout=30
    )
    print(f"   ✅ Status: {r.status_code}")
    if r.status_code == 200:
        print(f"   Response preview: {r.text[:150]}...")
    else:
        print(f"   Response: {r.text[:200]}")
except requests.exceptions.Timeout:
    print(f"   ⏱️  TIMEOUT - Azure OpenAI might be slow")
except Exception as e:
    print(f"   ❌ ERROR: {e}")

# Test 5: Roleplay start endpoint (THE IMPORTANT ONE!)
print("\n5. Testing /roleplay/start endpoint...")
print("   (This is what your Android app needs!)")
try:
    r = requests.post(
        f"{BASE_URL}/roleplay/start",
        json={
            "scenario_id": "job_interview",
            "student_name": "Test",
            "use_rag": True
        },
        timeout=30
    )
    print(f"   Status: {r.status_code}")
    if r.status_code == 200:
        data = r.json()
        print(f"   ✅ SUCCESS!")
        print(f"   Session ID: {data.get('session_id', 'N/A')[:16]}...")
        print(f"   Scenario: {data.get('scenario_title', 'N/A')}")
        print(f"   Initial message: {data.get('initial_message', 'N/A')[:80]}...")
    elif r.status_code == 404:
        print(f"   ❌ ENDPOINT NOT FOUND (404)")
        print(f"   The roleplay module is NOT deployed to Fly.io!")
    else:
        print(f"   ❌ FAILED with status {r.status_code}")
        print(f"   Response: {r.text[:500]}")
except requests.exceptions.Timeout:
    print(f"   ⏱️  TIMEOUT")
except Exception as e:
    print(f"   ❌ ERROR: {e}")

print("\n" + "=" * 60)
print("TEST COMPLETE")
print("=" * 60)
print("\nSUMMARY:")
print("- If test 5 shows 404, roleplay is NOT deployed")
print("- If test 5 shows 200, roleplay IS working!")
print("=" * 60)

