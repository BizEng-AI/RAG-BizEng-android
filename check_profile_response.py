import requests
import json

# First register to get tokens
print("1. Registering...")
reg_response = requests.post(
    'https://bizeng-server.fly.dev/auth/register',
    json={
        'email': 'test_profile_check@example.com',
        'password': 'Test123!',
        'display_name': 'Profile Test'
    },
    timeout=10
)

print(f"Registration Status: {reg_response.status_code}")
tokens = reg_response.json()
access_token = tokens.get('access_token')

print(f"\n2. Got access token: {access_token[:30]}...")

# Now call /me with the token
print("\n3. Calling /me endpoint...")
me_response = requests.get(
    'https://bizeng-server.fly.dev/me',
    headers={'Authorization': f'Bearer {access_token}'},
    timeout=10
)

print(f"Status: {me_response.status_code}")
print(f"\nResponse JSON:")
print(json.dumps(me_response.json(), indent=2))

# Check fields
data = me_response.json()
print(f"\nAvailable fields: {list(data.keys())}")
print(f"\nField types:")
for key, value in data.items():
    print(f"  {key}: {type(value).__name__} = {value}")

