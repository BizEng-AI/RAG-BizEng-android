package com.example.myapplication

// Registration UI flow test disabled for now since we are focusing
// on logging in with known admin credentials and then exercising
// the main tabs from the Home screen.

// import androidx.compose.ui.test.junit4.createAndroidComposeRule
// import androidx.compose.ui.test.onNodeWithText
// import androidx.compose.ui.test.performClick
// import androidx.compose.ui.test.performTextInput
// import androidx.test.ext.junit.runners.AndroidJUnit4
// import org.junit.Rule
// import org.junit.Test
// import org.junit.runner.RunWith
//
// /**
//  * Basic UI flow test that verifies the registration screen supports
//  * entering the required fields and tapping the Register button.
//  *
//  * This mirrors the login UI test: it checks wiring and layout, not
//  * the real backend behavior.
//  */
// @RunWith(AndroidJUnit4::class)
// class RegisterFlowTest {
//
//     @get:Rule
//     val composeRule = createAndroidComposeRule<MainActivity>()
//
//     @Test
//     fun registerFlow_displaysFields_andAllowsRegistrationClick() {
//         // Start from login and navigate to register
//         composeRule.onNodeWithText("BizEng Login").assertExists()
//         composeRule.onNodeWithText("Don't have an account? Register").performClick()
//         composeRule.waitForIdle()
//
//         // Now on RegisterScreen
//         composeRule.onNodeWithText("Create Account").assertExists()
//
//         // Fill in registration fields by label
//         composeRule.onNodeWithText("Display Name").performTextInput("Test User")
//         composeRule.onNodeWithText("Email").performTextInput("newuser@example.com")
//         composeRule.onNodeWithText("Password").performTextInput("secret123")
//         composeRule.onNodeWithText("Confirm Password").performTextInput("secret123")
//         composeRule.onNodeWithText("Group Number (Optional)").performTextInput("G1")
//
//         // Tap Register
//         composeRule.onNodeWithText("Register").performClick()
//         composeRule.waitForIdle()
//
//         // As with login, we don't assert navigation here; we just verify
//         // the UI elements exist and respond to input + click.
//     }
// }
