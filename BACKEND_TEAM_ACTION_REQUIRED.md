# 🚨 BACKEND TEAM - ACTION REQUIRED

**Priority:** HIGH  
**Blocking:** Admin Dashboard Students & Groups sections  
**Time Estimate:** 1-2 hours

---

## 🎯 What's Needed

Android client is **100% complete** and ready. We need **2 backend endpoints** deployed to enable the Students and Groups analytics sections.

---

## 📋 Quick Checklist

- [ ] **Step 1:** Run SQL migrations (5 min)
- [ ] **Step 2:** Add 2 Python endpoints (30 min)
- [ ] **Step 3:** Deploy to Fly.io (10 min)
- [ ] **Step 4:** Test with curl (5 min)
- [ ] **Step 5:** Notify Android team (1 min)

---

## 🗃️ Step 1: Database (5 min)

Run in Neon SQL console:

```sql
-- Add exercise tracking columns
ALTER TABLE exercise_attempts
    ADD COLUMN IF NOT EXISTS exercise_type TEXT,
    ADD COLUMN IF NOT EXISTS started_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS finished_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS duration_seconds INTEGER,
    ADD COLUMN IF NOT EXISTS pronunciation_score NUMERIC;

-- Add group support
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS group_name TEXT;

-- Add index for performance
CREATE INDEX IF NOT EXISTS idx_exercise_attempts_user_started 
    ON exercise_attempts(user_id, started_at);
```

---

## 💻 Step 2: Backend Code (30 min)

Add to your admin monitor router (e.g., `admin_monitor.py`):

```python
@router.get("/users_activity")
@admin_required
async def users_activity(days: int = 30, db: AsyncSession = Depends(get_db)):
    """Per-user exercise stats"""
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
        ORDER BY u.group_name NULLS LAST, u.email
    """), {"days": days})
    return [dict(r) for r in result.mappings().all()]

@router.get("/groups_activity")
@admin_required
async def groups_activity(days: int = 30, db: AsyncSession = Depends(get_db)):
    """Per-group exercise stats"""
    result = await db.execute(text("""
        SELECT
            COALESCE(u.group_name, 'Unassigned') AS group_name,
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
        ORDER BY u.group_name NULLS LAST
    """), {"days": days})
    return [dict(r) for r in result.mappings().all()]
```

**Note:** Make sure `@admin_required` decorator checks for "admin" role.

---

## 🚀 Step 3: Deploy (10 min)

```bash
cd /path/to/backend
fly deploy
```

---

## ✅ Step 4: Test (5 min)

```bash
# Get admin token
TOKEN=$(curl -s -X POST https://bizeng-server.fly.dev/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"yoo@gmail.com","password":"qwerty"}' | jq -r .access_token)

# Test users_activity (should return array, not 404)
curl -s "https://bizeng-server.fly.dev/admin/monitor/users_activity?days=30" \
  -H "Authorization: Bearer $TOKEN" | jq

# Test groups_activity (should return array, not 404)
curl -s "https://bizeng-server.fly.dev/admin/monitor/groups_activity?days=30" \
  -H "Authorization: Bearer $TOKEN" | jq
```

**Expected:** JSON arrays with user/group stats  
**Not Expected:** `{"detail":"Not Found"}`

---

## 📧 Step 5: Notify Android Team

Send message:
> ✅ Admin endpoints deployed:
> - `/admin/monitor/users_activity` 
> - `/admin/monitor/groups_activity`
> 
> Android team can now test Students & Groups sections in app.

---

## 🔍 What Data Format Android Expects

### `/users_activity` response:
```json
[
  {
    "user_id": 42,
    "email": "student@example.com",
    "display_name": "John Doe",
    "group_name": "Group A",
    "total_exercises": 25,
    "pronunciation_count": 10,
    "chat_count": 8,
    "roleplay_count": 7,
    "total_duration_seconds": 3600,
    "avg_pronunciation_score": 79.4
  }
]
```

### `/groups_activity` response:
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
  }
]
```

---

## ❓ FAQ

**Q: What if `exercise_attempts` is empty?**  
A: Both endpoints will return empty arrays `[]`, which is fine. Android shows "No data" message.

**Q: What if no groups are assigned?**  
A: Groups endpoint returns one entry with `group_name: "Unassigned"` for users with null `group_name`.

**Q: What about pagination?**  
A: Not needed yet. If you have 1000+ students in future, add `LIMIT` and `OFFSET` params.

**Q: Cache control?**  
A: Add response header: `Cache-Control: public, max-age=60` (same as other admin endpoints).

**Q: What about user_activity/{id} endpoint?**  
A: Not blocking. That's for a future "student detail" screen. Can add later.

---

## 🐛 Troubleshooting

**"Column does not exist"**  
→ Run Step 1 SQL migrations again

**"Permission denied"**  
→ Check `@admin_required` decorator is applied

**Empty arrays**  
→ Normal if no exercise data exists yet. Students need to complete exercises first.

**500 error**  
→ Check Fly logs: `fly logs -a bizeng-server`

---

## 📚 Reference Docs

Full details in:
- `ADMIN_ENDPOINTS_STATUS_REPORT.md` - Complete requirements
- `ADMIN_ANALYTICS_IMPLEMENTATION.md` - Android implementation
- `ADMIN_ANALYTICS_SUMMARY.md` - Full summary

---

## ⏱️ Time Breakdown

- SQL migrations: 5 min
- Copy/paste Python code: 10 min
- Test locally: 10 min
- Deploy to Fly: 10 min
- Verify with curl: 5 min
- **Total: ~40 minutes**

---

**Priority: HIGH** - Android app is ready and waiting for these 2 endpoints! 🚀

