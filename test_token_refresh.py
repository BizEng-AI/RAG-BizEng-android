import requests
import json
import time
from datetime import datetime

"""
Token Refresh End-to-End Test Script

This script tests the complete authentication and token refresh flow
against the live server at https://bizeng-server.fly.dev

Run with: python test_token_refresh.py
"""

BASE_URL = "https://bizeng-server.fly.dev"
TEST_EMAIL = f"test_{int(time.time())}@example.com"
TEST_PASSWORD = "TestPass123!"
TEST_NAME = "Test User"

class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    END = '\033[0m'

def log(message, color=Colors.BLUE):
    timestamp = datetime.now().strftime("%H:%M:%S")
    print(f"{color}[{timestamp}] {message}{Colors.END}")

def log_success(message):
    log(f"✅ {message}", Colors.GREEN)

def log_error(message):
    log(f"❌ {message}", Colors.RED)

def log_info(message):
    log(f"ℹ️  {message}", Colors.YELLOW)

def test_health():
    """Test if server is running"""
    log("Testing server health...")
    try:
        response = requests.get(f"{BASE_URL}/health", timeout=10)
        if response.status_code == 200:
            log_success("Server is healthy!")
            return True
        else:
            log_error(f"Server returned {response.status_code}")
            return False
    except Exception as e:
        log_error(f"Cannot reach server: {e}")
        return False

def test_register():
    """Test user registration"""
    log(f"Registering new user: {TEST_EMAIL}")
    try:
        response = requests.post(
            f"{BASE_URL}/auth/register",
            json={
                "email": TEST_EMAIL,
                "password": TEST_PASSWORD,
                "display_name": TEST_NAME
            },
            timeout=15
        )

        if response.status_code in [200, 201]:  # Accept both 200 OK and 201 Created
            data = response.json()
            if "access_token" in data and "refresh_token" in data:
                log_success("Registration successful!")
                log_info(f"Access token: {data['access_token'][:30]}...")
                log_info(f"Refresh token: {data['refresh_token'][:30]}...")
                return data
            else:
                log_error("Response missing tokens!")
                log_error(f"Response: {response.text}")
                return None
        else:
            log_error(f"Registration failed: {response.status_code}")
            log_error(f"Response: {response.text}")
            return None
    except Exception as e:
        log_error(f"Registration error: {e}")
        return None

def test_login():
    """Test user login"""
    log(f"Logging in as: {TEST_EMAIL}")
    try:
        response = requests.post(
            f"{BASE_URL}/auth/login",
            json={
                "email": TEST_EMAIL,
                "password": TEST_PASSWORD
            },
            timeout=15
        )

        if response.status_code == 200:
            data = response.json()
            if "access_token" in data and "refresh_token" in data:
                log_success("Login successful!")
                return data
            else:
                log_error("Response missing tokens!")
                return None
        else:
            log_error(f"Login failed: {response.status_code}")
            log_error(f"Response: {response.text}")
            return None
    except Exception as e:
        log_error(f"Login error: {e}")
        return None

def test_authenticated_request(access_token):
    """Test making an authenticated request"""
    log("Testing authenticated request to /me endpoint...")
    try:
        response = requests.get(
            f"{BASE_URL}/me",
            headers={"Authorization": f"Bearer {access_token}"},
            timeout=15
        )

        if response.status_code == 200:
            data = response.json()
            log_success("Authenticated request successful!")
            log_info(f"User: {data.get('display_name')} ({data.get('email')})")
            log_info(f"Roles: {data.get('roles')}")
            return True
        else:
            log_error(f"Request failed: {response.status_code}")
            log_error(f"Response: {response.text}")
            return False
    except Exception as e:
        log_error(f"Request error: {e}")
        return False

def test_token_refresh(refresh_token):
    """Test token refresh"""
    log("Testing token refresh...")
    try:
        response = requests.post(
            f"{BASE_URL}/auth/refresh",
            json={"refresh_token": refresh_token},
            timeout=15
        )

        if response.status_code == 200:
            data = response.json()
            if "access_token" in data and "refresh_token" in data:
                log_success("Token refresh successful!")
                log_info(f"New access token: {data['access_token'][:30]}...")
                log_info(f"New refresh token: {data['refresh_token'][:30]}...")
                return data
            else:
                log_error("Refresh response missing tokens!")
                return None
        else:
            log_error(f"Token refresh failed: {response.status_code}")
            log_error(f"Response: {response.text}")
            return None
    except Exception as e:
        log_error(f"Refresh error: {e}")
        return None

def test_expired_token():
    """Test request with invalid/expired token"""
    log("Testing request with invalid token (should return 401)...")
    try:
        response = requests.get(
            f"{BASE_URL}/me",
            headers={"Authorization": "Bearer invalid_token_xyz"},
            timeout=15
        )

        if response.status_code == 401:
            log_success("Server correctly rejected invalid token (401)!")
            return True
        else:
            log_error(f"Expected 401, got {response.status_code}")
            return False
    except Exception as e:
        log_error(f"Request error: {e}")
        return False

def test_logout(refresh_token):
    """Test logout (revoke refresh token)"""
    log("Testing logout...")
    try:
        response = requests.post(
            f"{BASE_URL}/auth/logout",
            json={"refresh_token": refresh_token},
            timeout=15
        )

        if response.status_code == 200:
            log_success("Logout successful!")
            return True
        else:
            log_error(f"Logout failed: {response.status_code}")
            return False
    except Exception as e:
        log_error(f"Logout error: {e}")
        return False

def test_chat_endpoint(access_token):
    """Test chat endpoint (requires authentication)"""
    log("Testing chat endpoint...")
    try:
        response = requests.post(
            f"{BASE_URL}/chat",
            headers={"Authorization": f"Bearer {access_token}"},
            json={
                "messages": [
                    {"role": "user", "content": "Hello, this is a test message"}
                ]
            },
            timeout=60  # Chat might take longer
        )

        if response.status_code == 200:
            data = response.json()
            log_success("Chat request successful!")
            log_info(f"Response: {data.get('answer', '')[:100]}...")
            return True
        else:
            log_error(f"Chat failed: {response.status_code}")
            log_error(f"Response: {response.text[:200]}")
            return False
    except Exception as e:
        log_error(f"Chat error: {e}")
        return False

def main():
    print("\n" + "="*60)
    print("🔐 TOKEN REFRESH END-TO-END TEST")
    print("="*60 + "\n")

    results = []

    # Test 1: Server Health
    log("TEST 1: Server Health Check")
    results.append(("Server Health", test_health()))
    print()

    if not results[-1][1]:
        log_error("Server is not reachable. Stopping tests.")
        return

    # Test 2: Registration
    log("TEST 2: User Registration")
    auth_data = test_register()
    results.append(("Registration", auth_data is not None))
    print()

    if not auth_data:
        log_error("Registration failed. Stopping tests.")
        return

    access_token = auth_data["access_token"]
    refresh_token = auth_data["refresh_token"]

    # Test 3: Authenticated Request
    log("TEST 3: Authenticated Request")
    results.append(("Auth Request", test_authenticated_request(access_token)))
    print()

    # Test 4: Token Refresh
    log("TEST 4: Token Refresh")
    new_tokens = test_token_refresh(refresh_token)
    results.append(("Token Refresh", new_tokens is not None))
    print()

    if new_tokens:
        new_access_token = new_tokens["access_token"]
        new_refresh_token = new_tokens["refresh_token"]

        # Test 5: Use New Token
        log("TEST 5: Use New Access Token")
        results.append(("New Token Works", test_authenticated_request(new_access_token)))
        print()

        # Test 6: Chat Endpoint
        log("TEST 6: Chat Endpoint (Authenticated)")
        results.append(("Chat Endpoint", test_chat_endpoint(new_access_token)))
        print()

    # Test 7: Invalid Token
    log("TEST 7: Invalid Token Handling")
    results.append(("Invalid Token", test_expired_token()))
    print()

    # Test 8: Logout
    log("TEST 8: Logout (Revoke Token)")
    logout_token = new_refresh_token if new_tokens else refresh_token
    results.append(("Logout", test_logout(logout_token)))
    print()

    # Summary
    print("\n" + "="*60)
    print("📊 TEST RESULTS SUMMARY")
    print("="*60 + "\n")

    passed = 0
    failed = 0

    for test_name, result in results:
        status = "✅ PASS" if result else "❌ FAIL"
        color = Colors.GREEN if result else Colors.RED
        print(f"{color}{status}{Colors.END} - {test_name}")
        if result:
            passed += 1
        else:
            failed += 1

    print("\n" + "="*60)
    print(f"Total: {passed} passed, {failed} failed out of {len(results)} tests")
    print("="*60 + "\n")

    if failed == 0:
        log_success("🎉 ALL TESTS PASSED! Token refresh is working correctly!")
    else:
        log_error(f"⚠️  {failed} test(s) failed. Please review the errors above.")

    print("\n💡 Next Steps:")
    print("  1. If all tests passed, the server is ready!")
    print("  2. Test the Android app with the manual testing guide")
    print("  3. Wait 31 minutes and test token auto-refresh in the app")
    print()

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n⚠️  Tests interrupted by user")
    except Exception as e:
        log_error(f"Unexpected error: {e}")
        import traceback
        traceback.print_exc()

