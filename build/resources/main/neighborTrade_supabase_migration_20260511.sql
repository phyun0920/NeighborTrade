-- NeighborTrade Supabase migration
-- 목적: 기존 Supabase PostgreSQL DB에 소셜 로그인용 member 컬럼을 추가한다.
-- 실행 위치: Supabase SQL Editor
-- 주의: 기존 데이터를 삭제하지 않는 ALTER 전용 스크립트이다.

ALTER TABLE member
    ADD COLUMN IF NOT EXISTS login_type VARCHAR(20) NOT NULL DEFAULT 'LOCAL';

ALTER TABLE member
    ADD COLUMN IF NOT EXISTS provider VARCHAR(30);

ALTER TABLE member
    ADD COLUMN IF NOT EXISTS provider_id VARCHAR(100);

ALTER TABLE member
    ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_member_login_type'
    ) THEN
        ALTER TABLE member
            ADD CONSTRAINT chk_member_login_type
            CHECK (login_type IN ('LOCAL', 'KAKAO', 'NAVER'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_member_provider_provider_id'
    ) THEN
        ALTER TABLE member
            ADD CONSTRAINT uk_member_provider_provider_id
            UNIQUE (provider, provider_id);
    END IF;
END $$;

-- 확인 쿼리
SELECT
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'member'
ORDER BY ordinal_position;
