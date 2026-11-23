-- Datos locales de demostración. 
BEGIN;

INSERT INTO public.venues (code, name, city, address, capacity)
VALUES
    ('DEMO-SMR', 'Marina Convention Center', 'Santa Marta', 'Sector Marina', 800),
    ('DEMO-BOG', 'Centro Cultural Demo', 'Bogotá', 'Sector Centro', 400)
ON CONFLICT (code) DO NOTHING;

INSERT INTO public.artists (stage_name, country, genre)
VALUES
    ('Solar Beat (Demo)', 'Colombia', 'Electrónica'),
    ('Neon Waves (Demo)', 'Colombia', 'Indie'),
    ('Caribbean Sound (Demo)', 'Colombia', 'Fusión caribeña')
ON CONFLICT (stage_name) DO NOTHING;

INSERT INTO public.events (event_code, name, description, category, status, event_date, minimum_age, venue_id)
SELECT demo.code, demo.name, demo.description, demo.category, 'PUBLISHED',
       (CURRENT_DATE + demo.days_ahead) + TIME '19:00', demo.minimum_age, venue.id
FROM (VALUES
    ('DEMO-CMF', 'Caribbean Music Fest', 'Festival de electrónica y sonidos del Caribe.', 'MUSIC', 14, 18, 'DEMO-SMR'),
    ('DEMO-JAZZ', 'Noche de Jazz y Cultura', 'Concierto de jazz y fusión en vivo.', 'CULTURE', 21, 0, 'DEMO-BOG'),
    ('DEMO-INDIE', 'Encuentro Indie', 'Una noche de música independiente.', 'ENTERTAINMENT', 28, 0, 'DEMO-SMR')
) AS demo(code, name, description, category, days_ahead, minimum_age, venue_code)
JOIN public.venues AS venue ON venue.code = demo.venue_code
ON CONFLICT (event_code) DO NOTHING;

INSERT INTO public.event_artists (event_id, artist_id)
SELECT event.id, artist.id
FROM (VALUES
    ('DEMO-CMF', 'Solar Beat (Demo)'),
    ('DEMO-CMF', 'Caribbean Sound (Demo)'),
    ('DEMO-JAZZ', 'Caribbean Sound (Demo)'),
    ('DEMO-INDIE', 'Neon Waves (Demo)')
) AS demo(event_code, stage_name)
JOIN public.events AS event ON event.event_code = demo.event_code
JOIN public.artists AS artist ON artist.stage_name = demo.stage_name
ON CONFLICT (event_id, artist_id) DO NOTHING;

COMMIT;
