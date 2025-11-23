package com.example.myapplication

// FakeAuthModule temporarily disabled: we are not wiring a full
// Hilt TestInstallIn replacement yet. Tests will use the real
// auth wiring until we introduce a proper fake module that
// correctly replaces the production AuthStorage/AuthManager
// providers.

// import com.example.myapplication.data.local.AuthManager
// import com.example.myapplication.data.local.AuthStorage
// import com.example.myapplication.data.local.StoredUserInfo
// import dagger.Module
// import dagger.Provides
// import dagger.hilt.components.SingletonComponent
// import dagger.hilt.testing.TestInstallIn
// import javax.inject.Singleton
//
// @Module
// @TestInstallIn(
//     components = [SingletonComponent::class],
//     replaces = [/* Real module class that provides AuthStorage/AuthManager */]
// )
// object FakeAuthModule { ... }
