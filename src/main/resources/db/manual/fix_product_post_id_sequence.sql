-- product_post PK 중복 오류 시 (duplicate key ... Key (id)=(N) already exists)
-- 원인: SERIAL/IDENTITY 시퀀스가 테이블의 MAX(id)보다 뒤처진 경우
--
-- 일반적으로는 애플리케이션 기동 시 PostgreSqlIdentitySequenceSync 가 자동으로 맞춥니다.
-- DB만 단독으로 수정해야 할 때 아래를 실행하세요.

SELECT setval(
               pg_get_serial_sequence('product_post', 'id'),
               COALESCE((SELECT MAX(id) FROM product_post), 1)
       );
