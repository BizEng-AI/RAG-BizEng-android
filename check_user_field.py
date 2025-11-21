import requests
import json

# Test registration response
try:
    response = requests.post(
        'https://bizeng-server.fly.dev/auth/register',
        json={
            'email': 'test_check_user@example.com',
            'password': 'Test123!',
            'display_name': 'Test User'
        },
        timeout=10
    )

    print(f"Status: {response.status_code}")
    print(f"\nResponse JSON:")
    print(json.dumps(response.json(), indent=2))

    # Check if user field exists
    data = response.json()
    if 'user' in data:
        print("\n✅ Server DOES include 'user' field")
        print(f"User fields: {list(data['user'].keys())}")
    else:
        print("\n❌ Server DOES NOT include 'user' field")
        print(f"Available fields: {list(data.keys())}")

except Exception as e:
    print(f"Error: {e}")

