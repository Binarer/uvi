-- ============================================================
-- Оптимизационные индексы для uvi
-- ============================================================

-- 1. places: пространственный GIST-индекс для поиска ближайших мест
--    (findNearbyPlaces, findRecommendedPlaces)
CREATE INDEX IF NOT EXISTS idx_places_location
    ON places USING GIST (location);

-- 2. places: индекс по типу и активности
--    (findByTypeAndIsActiveTrue, findByIsActiveTrueOrderByCreatedAtDesc)
CREATE INDEX IF NOT EXISTS idx_places_type_active
    ON places (type, is_active);

-- 3. places: индекс по создателю
--    (findByCreatedBy)
CREATE INDEX IF NOT EXISTS idx_places_created_by
    ON places (created_by_id);

-- 4. user_interests: составной уникальный индекс (user_id + interest)
--    (existsByUserIdAndInterest, findByUserIdAndInterest)
CREATE INDEX IF NOT EXISTS idx_user_interests_user_interest
    ON user_interests (user_id, interest);

-- 5. user_interests: индекс по preference_level для сортировки
--    (findByUserIdOrderedByPreference)
CREATE INDEX IF NOT EXISTS idx_user_interests_preference
    ON user_interests (user_id, preference_level DESC);

-- 6. family_members: составной индекс по семье и активности
--    (findByFamilyAndIsActive, countActiveMembersByFamilyId)
CREATE INDEX IF NOT EXISTS idx_family_members_family_active
    ON family_members (family_id, is_active);

-- 7. family_members: индекс по пользователю
--    (existsByFamilyAndUserAndIsActive)
CREATE INDEX IF NOT EXISTS idx_family_members_user
    ON family_members (user_id);

-- 8. family_invitations: индекс по коду приглашения
--    (findByInvitationCode)
CREATE INDEX IF NOT EXISTS idx_invitations_code
    ON family_invitations (invitation_code);

-- 9. family_invitations: индекс по статусу и сроку действия
--    (findExpiredInvitations, existsActiveInvitation)
CREATE INDEX IF NOT EXISTS idx_invitations_status_expires
    ON family_invitations (status, expires_at);

-- 10. user_locations: составной индекс для последней позиции пользователя
--     (findTopByUserIdOrderByTimestampDesc)
CREATE INDEX IF NOT EXISTS idx_user_locations_user_timestamp
    ON user_locations (user_id, timestamp DESC);

-- 11. user_locations: пространственный индекс для ST_DWithin запросов
CREATE INDEX IF NOT EXISTS idx_user_locations_coordinates
    ON user_locations USING GIST (coordinates);

-- 12. devices: индекс по пользователю
--     (findByUserId, findByUserIdAndIsActive)
CREATE INDEX IF NOT EXISTS idx_devices_user_id
    ON devices (user_id);

-- 13. sms_verifications: индекс по номеру телефона и сроку действия
--     (findByPhoneNumberAndExpiresAtAfter)
CREATE INDEX IF NOT EXISTS idx_sms_verifications_phone_expires
    ON sms_verifications (phone_number, expires_at);

-- 14. users: индекс по номеру телефона
--     (findByPhoneNumberActive)
CREATE INDEX IF NOT EXISTS idx_users_phone_number
    ON users (phone_number);

-- 15. tags: индекс по popularity для getPopularTags
CREATE INDEX IF NOT EXISTS idx_tags_usage_count
    ON tags (usage_count DESC);
