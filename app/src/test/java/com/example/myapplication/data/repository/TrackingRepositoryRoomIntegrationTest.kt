package com.example.myapplication.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.data.local.db.BizEngDatabase
import com.example.myapplication.data.local.db.ExerciseAttemptDao
import com.example.myapplication.data.remote.TrackingApi
import com.example.myapplication.data.remote.dto.AttemptDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import io.mockk.coEvery
import io.mockk.clearAllMocks
import io.mockk.mockk

/**
 * Integration-style test that wires a real in-memory Room DB
 * to the real ExerciseAttemptDao + TrackingRepository.
 *
 * Goal: prove that attempts are actually persisted and can be read back.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Ignore("Room/Tracking integration test - disabled by default to avoid affecting main unit-test pipeline")
class TrackingRepositoryRoomIntegrationTest {

    private lateinit var db: BizEngDatabase
    private lateinit var attemptDao: ExerciseAttemptDao
    private lateinit var trackingApi: TrackingApi
    private lateinit var repository: TrackingRepository

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, BizEngDatabase::class.java)
            .allowMainThreadQueries() // fine for tests
            .build()
        attemptDao = db.attemptDao()

        trackingApi = mockk()
        repository = TrackingRepository(trackingApi, attemptDao)
    }

    @After
    fun tearDown() {
        db.close()
        clearAllMocks()
    }

    @Test
    fun `getRecentAttempts returns data that was cached into Room`() = runTest {
        // Given: remote API returns one attempt
        val remoteAttempts = listOf(
            AttemptDto(
                id = 42,
                userId = 7,
                exerciseType = "chat",
                exerciseId = "conv_1",
                startedAt = "2025-11-11T10:00:00Z",
                finishedAt = "2025-11-11T10:05:00Z",
                durationSeconds = 300,
                score = 0.9,
                passed = null,
                extraMetadata = null
            )
        )
        coEvery { trackingApi.getMyAttempts(limit = any(), offset = any(), days = any()) } returns remoteAttempts

        // When: we collect from getRecentAttempts, it should
        // 1) emit the (initially empty) cache
        // 2) fetch from network and write to DB
        // 3) next collection should see cached data

        // First collection: should emit empty list from DB
        val first = repository.getRecentAttempts(days = 30, limit = 50).first()
        assertEquals(0, first.size, "Initial DB should be empty")

        // Trigger refresh and cache write by collecting again
        val second = repository.getRecentAttempts(days = 30, limit = 50).first()

        // Because getRecentAttempts writes network result into Room and maps
        // ExerciseAttemptEntity -> AttemptDto when emitting, we expect to see
        // the same values we got from the mocked API (modulo localId).
        assertEquals(1, second.size)
        val dto = second.first()
        assertEquals(42, dto.id)
        assertEquals(7, dto.userId)
        assertEquals("chat", dto.exerciseType)
        assertEquals("conv_1", dto.exerciseId)
        assertEquals(300, dto.durationSeconds)
        assertEquals(0.9, dto.score)
    }

    @Test
    fun `getRecentAttempts reads from Room cache even without new network data`() = runTest {
        // Pre-populate DB via DAO to simulate existing cached attempts
        com.example.myapplication.data.local.db.ExerciseAttemptEntity(
            remoteId = 100,
            userId = 99,
            exerciseType = "roleplay",
            exerciseId = "scenario_1",
            startedAt = "2025-11-11T09:00:00Z",
            finishedAt = "2025-11-11T09:10:00Z",
            durationSeconds = 600,
            score = 0.8
        ).let { entity ->
            attemptDao.upsertAll(listOf(entity))
        }

        // Make sure network path would not add anything new
        coEvery { trackingApi.getMyAttempts(any(), any(), any()) } returns emptyList()

        // When: we collect once, we should see the cached row mapped to AttemptDto
        val result = repository.getRecentAttempts(days = 30, limit = 50).first()

        assertEquals(1, result.size)
        val dto = result.first()
        assertEquals(100, dto.id)
        assertEquals(99, dto.userId)
        assertEquals("roleplay", dto.exerciseType)
        assertEquals("scenario_1", dto.exerciseId)
        assertEquals(600, dto.durationSeconds)
        assertEquals(0.8, dto.score)
    }
}
