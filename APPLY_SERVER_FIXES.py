"""
AUTO-FIX SERVER FOR AZURE CONFIGURATION
This script will automatically fix your server files to use Azure OpenAI
"""

import os
import re
import sys
from pathlib import Path

# Navigate to parent directory (where server files should be)
SERVER_DIR = Path(__file__).parent.parent
os.chdir(SERVER_DIR)

print("=" * 60)
print("AUTOMATIC SERVER FIX TOOL")
print("=" * 60)
print(f"\nWorking directory: {SERVER_DIR}")
print()

# Step 1: Find roleplay_referee.py
print("Step 1: Looking for roleplay_referee.py...")
referee_file = None
for file in ["roleplay_referee.py", "server/roleplay_referee.py", "api/roleplay_referee.py"]:
    if os.path.exists(file):
        referee_file = file
        print(f"✓ Found: {file}")
        break

if not referee_file:
    print("❌ roleplay_referee.py not found!")
    print("Please tell me where it is located.")
else:
    # Check if it needs fixing
    with open(referee_file, 'r', encoding='utf-8') as f:
        content = f.read()

    if "AzureOpenAI" in content:
        print("✓ Already uses AzureOpenAI - no fix needed")
    else:
        print("⚠️  Does NOT use AzureOpenAI - needs fixing!")
        print("\nApplying fix...")

        # Fix the imports
        old_import = r"from openai import OpenAI"
        new_import = "from openai import OpenAI, AzureOpenAI"

        if old_import in content:
            content = content.replace(old_import, new_import)
            print("  ✓ Updated imports")

        # Fix the settings import
        settings_pattern = r"from settings import (.*?)(\n)"
        match = re.search(settings_pattern, content)
        if match:
            old_settings = match.group(0)
            # Add Azure settings
            new_settings = """from settings import (
    OPENAI_API_KEY,
    CHAT_MODEL,
    USE_AZURE,
    AZURE_OPENAI_KEY,
    AZURE_OPENAI_ENDPOINT,
    AZURE_OPENAI_API_VERSION,
    AZURE_OPENAI_CHAT_DEPLOYMENT
)
"""
            content = content.replace(old_settings, new_settings)
            print("  ✓ Updated settings import")

        # Fix the client initialization
        old_client = r"oai = OpenAI\(api_key=OPENAI_API_KEY\)"
        new_client = """if USE_AZURE:
    oai = AzureOpenAI(
        api_key=AZURE_OPENAI_KEY,
        azure_endpoint=AZURE_OPENAI_ENDPOINT,
        api_version=AZURE_OPENAI_API_VERSION
    )
    model_name = AZURE_OPENAI_CHAT_DEPLOYMENT
else:
    oai = OpenAI(api_key=OPENAI_API_KEY)
    model_name = CHAT_MODEL"""

        content = re.sub(old_client, new_client, content)
        print("  ✓ Updated client initialization")

        # Fix model references in API calls
        content = re.sub(
            r'model=CHAT_MODEL',
            'model=model_name',
            content
        )
        print("  ✓ Updated model references")

        # Backup original file
        backup_file = f"{referee_file}.backup"
        with open(backup_file, 'w', encoding='utf-8') as f:
            f.write(open(referee_file, 'r', encoding='utf-8').read())
        print(f"  ✓ Backed up to: {backup_file}")

        # Write fixed file
        with open(referee_file, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"  ✓ Fixed file saved: {referee_file}")

print()

# Step 2: Check settings file
print("Step 2: Checking settings configuration...")
settings_files = ["settings.py", ".env", "config.py"]
found_settings = None

for file in settings_files:
    if os.path.exists(file):
        found_settings = file
        print(f"✓ Found settings file: {file}")
        break

if found_settings:
    with open(found_settings, 'r', encoding='utf-8') as f:
        settings_content = f.read()

    print("\nChecking Azure configuration...")

    checks = {
        "USE_AZURE": "USE_AZURE",
        "AZURE_OPENAI_KEY": "AZURE_OPENAI_KEY",
        "AZURE_OPENAI_ENDPOINT": "AZURE_OPENAI_ENDPOINT",
        "AZURE_OPENAI_CHAT_DEPLOYMENT": "AZURE_OPENAI_CHAT_DEPLOYMENT",
        "AZURE_OPENAI_EMBEDDING_DEPLOYMENT": "AZURE_OPENAI_EMBEDDING_DEPLOYMENT"
    }

    missing = []
    for key, var in checks.items():
        if var in settings_content:
            # Try to extract the value
            match = re.search(f'{var}\\s*=\\s*["\']?(.*?)["\']?\\s*(?:#|\\n)', settings_content)
            if match:
                value = match.group(1).strip('"\'')
                if key == "USE_AZURE":
                    if value.lower() in ['true', '1', 'yes']:
                        print(f"  ✓ {key} = True")
                    else:
                        print(f"  ⚠️  {key} = {value} (should be True!)")
                elif key in ["AZURE_OPENAI_CHAT_DEPLOYMENT", "AZURE_OPENAI_EMBEDDING_DEPLOYMENT"]:
                    if value and value not in ["your-deployment", "gpt-4", "gpt-35-turbo"]:
                        print(f"  ✓ {key} = {value}")
                    else:
                        print(f"  ❌ {key} = {value} (likely WRONG - check Azure Portal!)")
                else:
                    if value and "your-" not in value:
                        print(f"  ✓ {key} configured")
                    else:
                        print(f"  ❌ {key} = {value} (needs to be set!)")
        else:
            missing.append(key)
            print(f"  ❌ {key} - NOT FOUND")

    if missing:
        print("\n⚠️  Missing Azure configuration variables!")
        print("Add these to your settings file:")
        print()
        for var in missing:
            if var == "USE_AZURE":
                print("USE_AZURE = True")
            else:
                print(f"{var} = 'your-value-here'")

print()
print("=" * 60)
print("FIX SCRIPT COMPLETE")
print("=" * 60)
print()
print("Next steps:")
print("1. If roleplay_referee.py was fixed, restart your server")
print("2. Check your Azure deployment names in Azure Portal")
print("3. Update settings file with correct deployment names")
print("4. Test the app and check logs")
print()

