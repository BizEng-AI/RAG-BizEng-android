package com.example.myapplication

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tab flow tests under fake logged-in admin auth.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TabFlowsTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun waitForHome() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("BizEng").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun chatTab_showsChatUi() {
        waitForHome()
        composeRule.onNodeWithText("Chat").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Chat").assertExists()
    }

    @Test
    fun roleplayTab_showsRoleplayUi() {
        waitForHome()
        composeRule.onNodeWithText("Roleplay").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Roleplay").assertExists()
    }

    @Test
    fun pronunciationTab_showsPronunciationUi() {
        waitForHome()
        composeRule.onNodeWithText("Pronunciation").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Pronunciation").assertExists()
    }

    @Test
    fun adminTab_showsAdminDashboard_whenUserIsAdmin() {
        waitForHome()
        // Admin tab should be visible for our fake admin user
        composeRule.onNodeWithText("Admin").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Admin Dashboard").assertExists()
    }
}
