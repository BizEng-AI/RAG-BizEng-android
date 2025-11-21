import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("token_type") val tokenType: String = "bearer"
)

fun main() {
    // This is the EXACT response format the server returns (from our test)
    val serverResponse = """
        {
          "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhbmRyb2lkX3Rlc3RfMTc2Mjg5MTMzNUBleGFtcGxlLmNvbSIsInJvbGVzIjpbInN0dWRlbnQiXSwiaWF0IjoxNzYyODkxMzQzLCJleHAiOjE3NjI4OTIyNDMsInR5cGUiOiJhY2Nlc3MifQ.4B4aj9pHpn12t6cCidYO0tN-cuDiFSMknzBUB2i5zXU",
          "refresh_token": "5e437c3564814387807278cc3c8fa1f6",
          "token_type": "bearer"
        }
    """.trimIndent()

    println("Testing TokenResponse parsing...")
    println("=" * 60)

    // Test 1: Basic Json config (like in AuthApi)
    try {
        val json1 = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
        val result = json1.decodeFromString<TokenResponse>(serverResponse)
        println("✅ Test 1 PASSED (Basic config)")
        println("   accessToken: ${if (result.accessToken == null) "NULL" else "length ${result.accessToken.length}"}")
        println("   refreshToken: ${if (result.refreshToken == null) "NULL" else "length ${result.refreshToken.length}"}")
    } catch (e: Exception) {
        println("❌ Test 1 FAILED: ${e.message}")
    }

    // Test 2: With explicitNulls = false (like in KtorClientProvider)
    try {
        val json2 = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
            explicitNulls = false
        }
        val result = json2.decodeFromString<TokenResponse>(serverResponse)
        println("✅ Test 2 PASSED (With explicitNulls = false)")
        println("   accessToken: ${if (result.accessToken == null) "NULL" else "length ${result.accessToken.length}"}")
        println("   refreshToken: ${if (result.refreshToken == null) "NULL" else "length ${result.refreshToken.length}"}")
    } catch (e: Exception) {
        println("❌ Test 2 FAILED: ${e.message}")
    }

    // Test 3: With encodeDefaults = true
    try {
        val json3 = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
            encodeDefaults = true
        }
        val result = json3.decodeFromString<TokenResponse>(serverResponse)
        println("✅ Test 3 PASSED (With encodeDefaults = true)")
        println("   accessToken: ${if (result.accessToken == null) "NULL" else "length ${result.accessToken.length}"}")
        println("   refreshToken: ${if (result.refreshToken == null) "NULL" else "length ${result.refreshToken.length}"}")
    } catch (e: Exception) {
        println("❌ Test 3 FAILED: ${e.message}")
    }

    // Test 4: Check field names directly
    println("\n" + "=" * 60)
    println("Checking raw JSON field names...")
    val jsonElement = Json.parseToJsonElement(serverResponse)
    println("JSON keys: ${jsonElement.jsonObject.keys}")
    println("access_token exists: ${"access_token" in jsonElement.jsonObject}")
    println("refresh_token exists: ${"refresh_token" in jsonElement.jsonObject}")
}

operator fun String.times(count: Int) = this.repeat(count)

