-- Insert Game Provider
INSERT INTO game_providers (name, code, integration_type, status, api_url, api_key, logo_url, created_at, updated_at)
VALUES (
    'House Games',
    'HOUSE',
    'IFRAME',
    'ACTIVE',
    'http://localhost:8080/games',
    'house-secret-key-2024',
    'http://localhost:8080/games/logo.png',
    NOW(),
    NOW()
) ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    updated_at = NOW();

-- Get the provider ID
DO $$
DECLARE
    provider_id_var bigint;
BEGIN
    SELECT id INTO provider_id_var FROM game_providers WHERE code = 'HOUSE';

    -- Insert Classic Fruit Slot Game
    INSERT INTO games (
        name,
        game_code,
        description,
        category,
        provider_id,
        iframe_url,
        thumbnail_url,
        rtp,
        featured,
        status,
        sort_order,
        created_at,
        updated_at
    ) VALUES (
        'Classic Fruit Slot',
        'FRUIT_SLOT_01',
        'Classic 3-reel fruit slot machine with exciting payouts and jackpots!',
        'SLOTS',
        provider_id_var,
        'http://localhost:8080/games/slots/first/index.html',
        'https://via.placeholder.com/300x200?text=Fruit+Slot',
        96.5,
        true,
        'ACTIVE',
        1,
        NOW(),
        NOW()
    ) ON CONFLICT (game_code) DO UPDATE SET
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        iframe_url = EXCLUDED.iframe_url,
        updated_at = NOW();
END $$;

-- Verify insertion
SELECT
    g.id,
    g.name,
    g.game_code,
    g.category,
    gp.name as provider_name,
    g.iframe_url,
    g.status
FROM games g
JOIN game_providers gp ON g.provider_id = gp.id
WHERE g.game_code = 'FRUIT_SLOT_01';
