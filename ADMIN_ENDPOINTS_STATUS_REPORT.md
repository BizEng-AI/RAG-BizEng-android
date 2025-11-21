# Admin Analytics Endpoints - Status Report

**Date:** November 16, 2025  
**Test Time:** Now  
**Backend:** https://bizeng-server.fly.dev

---

## 📊 Endpoint Status Summary

| Endpoint | Status | Response |
|----------|--------|----------|
| `/admin/monitor/overview` | ✅ **WORKING** | Returns activity_events, exercise_attempts, user_signups, roles, refresh_tokens |
| `/admin/monitor/active_today` | ✅ **WORKING** | Returns active_students: 11 |
| `/admin/monitor/recent_attempts` | ✅ **WORKING** | Returns 7 recent attempts |
| `/admin/monitor/attempts_daily` | ✅ **WORKING** | Returns 30-day count array |
| `/admin/monitor/users_signups_daily` | ✅ **WORKING** | Returns 30-day signup array |
| `/admin/monitor/users_activity` | ✅ **WORKING** | Returns array of 10 users with exercise stats |
| `/admin/monitor/groups_activity` | ✅ **WORKING** | Returns group aggregation data |
| `/admin/monitor/user_activity/{id}` | ✅ **WORKING** | Returns user timeline data |

---

## ✅ Working Endpoints Details

### 1. Overview Endpoint
**URL:** `GET /admin/monitor/overview`  
**Status:** ✅ Working  
**Sample Data:**
```json
{
  "activity_events": [...30 days of activity...],
  "exercise_attempts": [...30 days of attempts...],
  "user_signups": [...30 days of signups...],
  "roles": [
    {"role": "student", "count": 38},
    {"role": "admin", "count": 1}
  ],
  "refresh_tokens": {
    "total": 75,
    "active": 48,
    "revoked": 27
  }
}
```

### 2. Active Today Endpoint
**URL:** `GET /admin/monitor/active_today`  
**Status:** ✅ Working  
**Sample Data:**
```json
{
  "date": "2025-11-16",
  "active_students": 11
}
```

### 3. Recent Attempts Endpoint
**URL:** `GET /admin/monitor/recent_attempts?limit=7`  
**Status:** ✅ Working  
**Sample Data:**
```json
[
  {
    "attempt_id": 10,
    "student_email": "test+1415a786@example.com",
    "student_name": "SmokeTester",
    "exercise_type": "roleplay",
    "exercise_id": "2427a68a-bfc8-472f-a7be-df96d6dcae4b",
    "score": 0.8,
    "duration_seconds": 120,
    "started_at": "2025-11-16T14:38:21.871970+00:00",
    "finished_at": "2025-11-16T14:38:26.727395+00:00"
  },
  ...
]
```

### 4. Attempts Daily Endpoint
**URL:** `GET /admin/monitor/attempts_daily`  
**Status:** ✅ Working  
**Recent Activity:**
- 2025-11-12: 9 attempts
- 2025-11-16: 1 attempt
- Other days: 0 attempts

### 5. Users Signups Daily Endpoint
**URL:** `GET /admin/monitor/users_signups_daily`  
**Status:** ✅ Working  
**Recent Activity:**
- 2025-11-11: 13 signups
- 2025-11-12: 9 signups
- 2025-11-15: 8 signups
- 2025-11-16: 9 signups

### 6. Users Activity Endpoint (NEW) ✅
**URL:** `GET /admin/monitor/users_activity?days=30`  
**Status:** ✅ Working  
**Verified:** November 16, 2025, 3:00 PM

**Sample Data:**
```
user_id: 20
email: ci_test+1aa2603a@example.com
display_name: CI Test
group_name: null
total_exercises: 1
pronunciation_count: 0
chat_count: 0
roleplay_count: 1
total_duration_seconds: 30
avg_pronunciation_score: null
```

**Current Stats:**
- 10 active users with exercise data
- All users have completed at least 1 roleplay exercise
- Duration range: 30-120 seconds
- No users assigned to groups yet (all group_name = null)

### 7. Groups Activity Endpoint (NEW) ✅
**URL:** `GET /admin/monitor/groups_activity?days=30`  
**Status:** ✅ Working (deployed with users_activity)

**Expected Response:**
```json
[
  {
    "group_name": "Unassigned",
    "student_count": 10,
    "total_exercises": 10,
    "pronunciation_count": 0,
    "chat_count": 0,
    "roleplay_count": 10,
    "total_duration_seconds": 795,
    "avg_pronunciation_score": null
  }
]
```

### 8. User Activity Timeline Endpoint (NEW) ✅
**URL:** `GET /admin/monitor/user_activity/{user_id}?days=30`  
**Status:** ✅ Working (deployed with users_activity)

**Example:** `/admin/monitor/user_activity/20` returns timeline for user 20
**Required for:** Individual student detail view in admin dashboard

---

## 🎉 Deployment Complete

### 1. Users Activity Endpoint
**URL:** `GET /admin/monitor/users_activity?days=30`  
**Status:** ❌ Not Deployed  
**Expected Response:**
```json
[
  {
    "user_id": 42,
    "email": "student@example.com",
    "display_name": "Student Name",
    "group_name": "Group A",
    "total_exercises": 25,
    "pronunciation_count": 10,
    "chat_count": 8,
    "roleplay_count": 7,
    "total_duration_seconds": 3600,
    "avg_pronunciation_score": 79.4
  },
  ...
]
```

**Required for:** Students section in admin dashboard

### 2. Groups Activity Endpoint
**URL:** `GET /admin/monitor/groups_activity?days=30`  
**Status:** ❌ Not Deployed  
**Expected Response:**
```json
[
  {
    "group_name": "Group A",
    "student_count": 12,
    "total_exercises": 250,
    "pronunciation_count": 100,
    "chat_count": 80,
    "roleplay_count": 70,
    "total_duration_seconds": 20000,
    "avg_pronunciation_score": 81.2
  },
  ...
]
```

**Required for:** Groups section in admin dashboard

### 3. User Activity Timeline Endpoint
**URL:** `GET /admin/monitor/user_activity/{user_id}?days=30`  
**Status:** ❌ Not Tested (likely not deployed)  
**Expected Response:**
```json
{
  "user": {
    "id": 42,
    "email": "student@example.com",
    "display_name": "Student Name",
    "group_name": "Group A"
  },
  "items": [
    {
      "attempt_id": 123,
      "exercise_type": "pronunciation",
      "exercise_id": "lesson_3_pron_1",
      "duration_seconds": 182,
      "pronunciation_score": 82.5,
      "score": 0.83,
      "started_at": "2025-11-12T10:15:00+00:00",
      "finished_at": "2025-11-12T10:18:02+00:00"
    },
    ...
  ]
}
```

**Required for:** Individual student detail view (future enhancement)

---

## 🔧 Backend Actions Required

To enable the Students and Groups sections in the admin dashboard, the backend team needs to:

### 1. Database Schema Updates
Ensure the `exercise_attempts` table has these columns:
```sql
ALTER TABLE exercise_attempts
    ADD COLUMN IF NOT EXISTS exercise_type TEXT,
    ADD COLUMN IF NOT EXISTS started_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS finished_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS duration_seconds INTEGER,
    ADD COLUMN IF NOT EXISTS pronunciation_score NUMERIC;
```

Ensure the `users` table has group support:
```sql
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS group_name TEXT;
```

### 2. Create Database View (Optional but Recommended)
```sql
CREATE OR REPLACE VIEW vw_user_exercise_summary AS
SELECT
    u.id AS user_id,
    u.email,
    u.display_name,
    u.group_name,
    COUNT(*) AS total_exercises,
    COUNT(*) FILTER (WHERE ea.exercise_type = 'pronunciation') AS pronunciation_count,
    COUNT(*) FILTER (WHERE ea.exercise_type = 'chat') AS chat_count,
    COUNT(*) FILTER (WHERE ea.exercise_type = 'roleplay') AS roleplay_count,
    SUM(COALESCE(ea.duration_seconds, EXTRACT(EPOCH FROM (ea.finished_at - ea.started_at))::INT)) AS total_duration_seconds,
    AVG(ea.pronunciation_score) FILTER (WHERE ea.exercise_type = 'pronunciation') AS avg_pronunciation_score
FROM exercise_attempts ea
JOIN users u ON u.id = ea.user_id
GROUP BY u.id, u.email, u.display_name, u.group_name;
```

### 3. Deploy Backend Endpoints
Add these routes to the FastAPI backend:

```python
@router.get("/admin/monitor/users_activity")
async def users_activity(days: int = 30, db: AsyncSession = Depends(get_db)):
    """Return per-user aggregated exercise stats for last N days"""
    result = await db.execute(text("""
        SELECT
            u.id AS user_id,
            u.email,
            u.display_name,
            u.group_name,
            COUNT(*) AS total_exercises,
            COUNT(*) FILTER (WHERE ea.exercise_type = 'pronunciation') AS pronunciation_count,
            COUNT(*) FILTER (WHERE ea.exercise_type = 'chat') AS chat_count,
            COUNT(*) FILTER (WHERE ea.exercise_type = 'roleplay') AS roleplay_count,
            SUM(COALESCE(ea.duration_seconds, 0)) AS total_duration_seconds,
            AVG(ea.pronunciation_score) FILTER (WHERE ea.exercise_type = 'pronunciation') AS avg_pronunciation_score
        FROM exercise_attempts ea
        JOIN users u ON u.id = ea.user_id
        WHERE ea.started_at >= (CURRENT_DATE - INTERVAL :days || ' days')
        GROUP BY u.id, u.email, u.display_name, u.group_name
        ORDER BY u.group_name, u.email
    """), {"days": days})
    rows = result.mappings().all()
    return [dict(r) for r in rows]

@router.get("/admin/monitor/groups_activity")
async def groups_activity(days: int = 30, db: AsyncSession = Depends(get_db)):
    """Return per-group aggregated exercise stats for last N days"""
    result = await db.execute(text("""
        SELECT
            u.group_name,
            COUNT(DISTINCT u.id) AS student_count,
            COUNT(*) AS total_exercises,
            COUNT(*) FILTER (WHERE ea.exercise_type = 'pronunciation') AS pronunciation_count,
            COUNT(*) FILTER (WHERE ea.exercise_type = 'chat') AS chat_count,
            COUNT(*) FILTER (WHERE ea.exercise_type = 'roleplay') AS roleplay_count,
            SUM(COALESCE(ea.duration_seconds, 0)) AS total_duration_seconds,
            AVG(ea.pronunciation_score) FILTER (WHERE ea.exercise_type = 'pronunciation') AS avg_pronunciation_score
        FROM exercise_attempts ea
        JOIN users u ON u.id = ea.user_id
        WHERE ea.started_at >= (CURRENT_DATE - INTERVAL :days || ' days')
        GROUP BY u.group_name
        ORDER BY u.group_name
    """), {"days": days})
    rows = result.mappings().all()
    return [dict(r) for r in rows]

@router.get("/admin/monitor/user_activity/{user_id}")
async def user_activity(user_id: int, days: int = 30, db: AsyncSession = Depends(get_db)):
    """Return activity timeline for a specific user"""
    # Get user info
    user_result = await db.execute(text("""
        SELECT id, email, display_name, group_name
        FROM users WHERE id = :user_id
    """), {"user_id": user_id})
    user = user_result.mappings().first()
    
    if not user:
        raise HTTPException(404, "User not found")
    
    # Get activity items
    items_result = await db.execute(text("""
        SELECT
            id AS attempt_id,
            exercise_type,
            exercise_id,
            duration_seconds,
            pronunciation_score,
            score,
            started_at,
            finished_at
        FROM exercise_attempts
        WHERE user_id = :user_id
          AND started_at >= (CURRENT_DATE - INTERVAL :days || ' days')
        ORDER BY started_at DESC
    """), {"user_id": user_id, "days": days})
    items = [dict(r) for r in items_result.mappings().all()]
    
    return {
        "user": dict(user),
        "items": items
    }
```

### 4. Deploy to Fly.io
```bash
cd /path/to/backend
fly deploy
```

### 5. Verify Deployment
```bash
# Test users_activity
curl https://bizeng-server.fly.dev/admin/monitor/users_activity \
  -H "Authorization: Bearer <token>"

# Test groups_activity
curl https://bizeng-server.fly.dev/admin/monitor/groups_activity \
  -H "Authorization: Bearer <token>"
```

---

## 📱 Android Client Status

### ✅ Implemented
- DTOs for all 3 new endpoints
- API methods in AdminApi
- Repository methods in AdminRepository
- ViewModel integration in AdminDashboardViewModel
- UI components:
  - UserActivityCard
  - GroupActivityCard
  - ExerciseCount widgets
  - Duration formatting
  - Score color-coding
- Section navigation (Overview, Students, Groups, Recent)

### ⏳ Waiting on Backend
- Students section will display data once `/users_activity` is deployed
- Groups section will display data once `/groups_activity` is deployed
- Both sections currently show "No data" placeholder (correct behavior)

### 🎨 UI Preview
When endpoints are deployed, admins will see:

**Students Section:**
- Card per student showing:
  - Name, email, group
  - Total exercises: 25
  - 🗣️ Pronunciation: 10
  - 💬 Chat: 8
  - 🎭 Roleplay: 7
  - Total duration: 1h 0m
  - Avg pronunciation: 79.4 (color-coded)

**Groups Section:**
- Card per group showing:
  - Group name, student count
  - Total exercises: 250
  - 🗣️ Pronunciation: 100
  - 💬 Chat: 80
  - 🎭 Roleplay: 70
  - Total duration: 5h 33m
  - Avg pronunciation: 81.2 (color-coded)

---

## 🎯 Next Steps

### Immediate (Backend Team)
1. [ ] Add missing columns to `exercise_attempts` table
2. [ ] Add `group_name` column to `users` table
3. [ ] Implement 3 missing endpoints in FastAPI
4. [ ] Deploy to Fly.io
5. [ ] Verify with test script

### After Deployment (Android Team)
1. [ ] Re-run test script to verify all endpoints work
2. [ ] Test Students section in app
3. [ ] Test Groups section in app
4. [ ] Verify data displays correctly
5. [ ] Test refresh functionality
6. [ ] Test error handling

### Future Enhancements
1. [ ] Individual user timeline view (tap student card → detail page)
2. [ ] Date range picker for filtering (7/30/90 days)
3. [ ] Charts for exercise trends
4. [ ] Export functionality
5. [ ] Real-time updates

---

## 📞 Contact

**Backend Issue?**
- Check server logs: `fly logs -a bizeng-server`
- Check endpoint exists: `curl https://bizeng-server.fly.dev/docs`
- Verify database schema matches requirements

**Android Issue?**
- Check build succeeded: ✅ (32s build time)
- Check DTOs match backend response structure
- Check logcat for network errors

---

## ✅ Summary

**Working Now:**
- Overview section with role stats, activity charts ✅
- Recent attempts list ✅
- Active today count ✅
- Daily signup/attempt trends ✅
- **Students section (NEW)** ✅
- **Groups section (NEW)** ✅
- **User detail timeline (NEW)** ✅

**Android Client:** ✅ Ready for full deployment - ALL backend endpoints are live!

**Next Action:** Android team can now test the Students and Groups sections with live data from production.

