INSERT INTO "configuration" ("id", "created_time", "version", "keyname", "valuefield", "category", "datatype")
VALUES (
    gen_random_uuid(),
    now(),
    0,
    'publicHolidaysApiUrl',
    'https://openholidaysapi.org/PublicHolidays?countryIsoCode=DE&languageIsoCode=DE&subdivisionCode=DE-NI&validFrom={validFrom}&validTo={validTo}',
    'dave',
    'STRING'
)
ON CONFLICT (keyname) DO NOTHING;
