INSERT INTO "configuration" ("id", "created_time", "version", "keyname", "valuefield", "category", "datatype")
VALUES (
    gen_random_uuid(),
    now(),
    0,
    'schoolHolidaysApiUrl',
    'https://openholidaysapi.org/SchoolHolidays?countryIsoCode=DE&validFrom={validFrom}&validTo={validTo}&languageIsoCode=DE&subdivisionCode=DE-NI',
    'dave',
    'STRING'
)
ON CONFLICT (keyname) DO NOTHING;
