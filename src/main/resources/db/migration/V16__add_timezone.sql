ALTER TABLE zeitintervall
    ALTER COLUMN endeuhrzeit 
    TYPE TIMESTAMP WITH TIME ZONE
        USING endeuhrzeit AT TIME ZONE 'UTC';

ALTER TABLE zeitintervall
    ALTER COLUMN startuhrzeit 
    TYPE TIMESTAMP WITH TIME ZONE
        USING startuhrzeit AT TIME ZONE 'UTC';