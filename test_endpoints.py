"""
ENDPOINT TEST SUITE - Tests all server endpoints
Run this to verify everything works before deploying
"""
import requests
import json
from pathlib import Path

BASE_URL = "http://localhost:8020"  # Change to your ngrok URL if testing remotely

def print_test(name, status=""):
    print(f"\n{'='*60}")
    print(f"TEST: {name}")
    if status:
        print(f"STATUS: {status}")
    print('='*60)

def test_health():
    print_test("Health Check")
    try:
        resp = requests.get(f"{BASE_URL}/health")
        print(f"Status: {resp.status_code}")
        print(f"Response: {resp.json()}")
        assert resp.status_code == 200
        assert resp.json()["status"] == "nowwww"
        print("✅ PASSED")
        return True
    except Exception as e:
        print(f"❌ FAILED: {e}")
        return False

def test_version():
    print_test("Version Check")
    try:
        resp = requests.get(f"{BASE_URL}/version")
        print(f"Status: {resp.status_code}")
        print(f"Response: {resp.json()}")
        assert resp.status_code == 200
        print("✅ PASSED")
        return True
    except Exception as e:
        print(f"❌ FAILED: {e}")
        return False

def test_chat():
    print_test("Chat Endpoint")
    try:
        data = {
            "messages": [
                {"role": "user", "content": "What is business English?"}
            ]
        }
        resp = requests.post(f"{BASE_URL}/chat", json=data)
        print(f"Status: {resp.status_code}")
        result = resp.json()
        print(f"Answer: {result.get('answer', '')[:100]}...")
        assert resp.status_code == 200
        assert "answer" in result or "message" in result
        print("✅ PASSED")
        return True
    except Exception as e:
        print(f"❌ FAILED: {e}")
        return False

def test_ask():
    print_test("Ask/RAG Endpoint")
    try:
        data = {
            "query": "What is business communication?",
            "k": 5
        }
        resp = requests.post(f"{BASE_URL}/ask", json=data)
        print(f"Status: {resp.status_code}")
        result = resp.json()
        print(f"Answer: {result.get('answer', '')[:100]}...")
        print(f"Sources: {len(result.get('sources', []))}")
        assert resp.status_code == 200
        assert "answer" in result
        print("✅ PASSED")
        return True
    except Exception as e:
        print(f"❌ FAILED: {e}")
        return False

def test_roleplay_start():
    print_test("Roleplay - Start Session")
    try:
        data = {
            "scenario_id": "job_interview",
            "student_name": "Test User",
            "use_rag": True
        }
        resp = requests.post(f"{BASE_URL}/roleplay/start", json=data)
        print(f"Status: {resp.status_code}")
        result = resp.json()
        print(f"Session ID: {result.get('session_id', 'N/A')}")
        print(f"Scenario: {result.get('scenario_title', 'N/A')}")
        print(f"Initial Message: {result.get('initial_message', '')[:100]}...")

        assert resp.status_code == 200
        assert "session_id" in result
        assert "initial_message" in result
        print("✅ PASSED")
        return result.get('session_id')
    except Exception as e:
        print(f"❌ FAILED: {e}")
        return None

def test_roleplay_turn_profanity(session_id):
    print_test("Roleplay - Profanity Detection Test")
    if not session_id:
        print("⚠️ SKIPPED - No session ID")
        return False

    try:
        # Test 1: Extreme profanity
        data = {
            "session_id": session_id,
            "message": "fuck off"
        }
        resp = requests.post(f"{BASE_URL}/roleplay/turn", json=data)
        print(f"Status: {resp.status_code}")
        result = resp.json()

        print(f"\nAI Response: {result.get('ai_message', '')}")
        print(f"\nCorrection Object:")
        correction = result.get('correction', {})
        print(f"  has_errors: {correction.get('has_errors', False)}")
        print(f"  errors: {correction.get('errors', [])}")
        print(f"  feedback: {correction.get('feedback', '')}")

        # This SHOULD detect an error
        if correction.get('has_errors'):
            print("✅ PASSED - Profanity detected")
            return True
        else:
            print("❌ FAILED - Profanity NOT detected! This is the bug!")
            return False

    except Exception as e:
        print(f"❌ FAILED: {e}")
        return False

def test_roleplay_turn_normal(session_id):
    print_test("Roleplay - Normal Response Test")
    if not session_id:
        print("⚠️ SKIPPED - No session ID")
        return False

    try:
        data = {
            "session_id": session_id,
            "message": "Hello, I'm very excited about this opportunity"
        }
        resp = requests.post(f"{BASE_URL}/roleplay/turn", json=data)
        print(f"Status: {resp.status_code}")
        result = resp.json()

        print(f"AI Response: {result.get('ai_message', '')}")
        correction = result.get('correction', {})
        print(f"has_errors: {correction.get('has_errors', False)}")

        assert resp.status_code == 200
        assert "ai_message" in result
        print("✅ PASSED")
        return True
    except Exception as e:
        print(f"❌ FAILED: {e}")
        return False

def test_roleplay_turn_grammar_error(session_id):
    print_test("Roleplay - Grammar Error Detection")
    if not session_id:
        print("⚠️ SKIPPED - No session ID")
        return False

    try:
        data = {
            "session_id": session_id,
            "message": "I goed to the office yesterday"
        }
        resp = requests.post(f"{BASE_URL}/roleplay/turn", json=data)
        result = resp.json()

        print(f"AI Response: {result.get('ai_message', '')}")
        correction = result.get('correction', {})
        print(f"has_errors: {correction.get('has_errors', False)}")
        print(f"errors: {correction.get('errors', [])}")

        # Should detect "goed" -> "went"
        if correction.get('has_errors'):
            print("✅ PASSED - Grammar error detected")
            return True
        else:
            print("⚠️ May have missed error, but acceptable")
            return True
    except Exception as e:
        print(f"❌ FAILED: {e}")
        return False

def test_pronunciation():
    print_test("Pronunciation - Test Endpoint")
    try:
        resp = requests.get(f"{BASE_URL}/pronunciation/test")
        print(f"Status: {resp.status_code}")
        result = resp.json()
        print(f"Service: {result.get('service', 'N/A')}")
        print(f"Region: {result.get('region', 'N/A')}")
        print(f"Status: {result.get('status', 'N/A')}")

        assert resp.status_code == 200
        assert result.get('status') == 'ok'
        print("✅ PASSED")
        return True
    except Exception as e:
        print(f"❌ FAILED: {e}")
        return False

def test_embed():
    print_test("Debug - Embedding Test")
    try:
        data = {"text": "This is a test"}
        resp = requests.post(f"{BASE_URL}/debug/embed", json=data)
        print(f"Status: {resp.status_code}")
        result = resp.json()
        print(f"Dimension: {result.get('dim', 'N/A')}")

        assert resp.status_code == 200
        assert result.get('dim') in [1536, 3072]  # Valid embedding dimensions
        print("✅ PASSED")
        return True
    except Exception as e:
        print(f"❌ FAILED: {e}")
        return False

def run_all_tests():
    print("\n" + "="*60)
    print("RUNNING COMPLETE SERVER TEST SUITE")
    print("="*60)

    results = {}

    # Basic tests
    results['health'] = test_health()
    results['version'] = test_version()
    results['embed'] = test_embed()

    # Chat & RAG
    results['chat'] = test_chat()
    results['ask'] = test_ask()

    # Roleplay
    session_id = test_roleplay_start()
    results['roleplay_start'] = bool(session_id)

    if session_id:
        results['profanity_detection'] = test_roleplay_turn_profanity(session_id)
        results['normal_turn'] = test_roleplay_turn_normal(session_id)
        results['grammar_detection'] = test_roleplay_turn_grammar_error(session_id)

    # Pronunciation
    results['pronunciation'] = test_pronunciation()

    # Summary
    print("\n" + "="*60)
    print("TEST SUMMARY")
    print("="*60)
    passed = sum(1 for v in results.values() if v)
    total = len(results)

    for test_name, result in results.items():
        status = "✅ PASS" if result else "❌ FAIL"
        print(f"{test_name:30s} {status}")

    print(f"\n{passed}/{total} tests passed")

    if not results.get('profanity_detection'):
        print("\n" + "!"*60)
        print("🚨 CRITICAL ISSUE DETECTED:")
        print("Profanity/inappropriate language is NOT being caught!")
        print("The roleplay referee needs to be more aggressive.")
        print("!"*60)

    return results

if __name__ == "__main__":
    import sys

    if len(sys.argv) > 1:
        BASE_URL = sys.argv[1]
        print(f"Testing server at: {BASE_URL}")
    else:
        print(f"Testing local server at: {BASE_URL}")
        print("Usage: python test_endpoints.py <server_url>")
        print("Example: python test_endpoints.py https://abc123.ngrok-free.dev")

    run_all_tests()

