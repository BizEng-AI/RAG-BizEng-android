package com.example.myapplication

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Login UI flow test currently disabled for release; kept here
 * for future regression testing when we re-enable instrumentation
 * UI flows.
 */
// @HiltAndroidTest
// @RunWith(AndroidJUnit4::class)
// class LoginFlowTest {
//
//     @get:Rule(order = 0)
//     val hiltRule = HiltAndroidRule(this)
//
//     @get:Rule(order = 1)
//     val composeRule = createAndroidComposeRule<MainActivity>()
//
//     @Test
//     fun login_withAdminCredentials_navigatesToHome() {
//         // Wait until we either see the Login screen or Home.
//         composeRule.waitUntil(timeoutMillis = 20_000) {
//             composeRule.onAllNodesWithText("BizEng Login").fetchSemanticsNodes().isNotEmpty() ||
//             composeRule.onAllNodesWithText("BizEng").fetchSemanticsNodes().isNotEmpty()
//         }
//
//         // If we're already on Home (BizEng top bar, no BizEng Login), we're done.
//         val alreadyHome =
//             composeRule.onAllNodesWithText("BizEng").fetchSemanticsNodes().isNotEmpty() &&
//             composeRule.onAllNodesWithText("BizEng Login").fetchSemanticsNodes().isEmpty()
//
//         if (!alreadyHome) {
//             // Enter login creds on the login screen.
//             composeRule.onNodeWithText("BizEng Login").assertExists()
//             composeRule.onNodeWithText("Email").performTextInput("yoo@gmail.com")
//             composeRule.onNodeWithText("Password").performTextInput("qwerty")
//             composeRule.onNodeWithText("Login").performClick()
//         }
//
//         // Now wait for Home: BizEng app bar present, login title gone.
//         composeRule.waitUntil(timeoutMillis = 30_000) {
//             composeRule.onAllNodesWithText("BizEng").fetchSemanticsNodes().isNotEmpty() &&
//             composeRule.onAllNodesWithText("BizEng Login").fetchSemanticsNodes().isEmpty()
//         }
//
//         composeRule.onNodeWithText("BizEng").assertExists()
//     }
// }
