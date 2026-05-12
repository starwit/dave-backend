

CREATE TABLE IF NOT EXISTS "zaehlstelleimage"
(
    "id" character varying(36) NOT NULL,
    "created_time" timestamp without time zone NOT NULL,
    "version" bigint,
    "name" VARCHAR(255) NOT NULL,
    "contenttype" VARCHAR(255),
    "imagedata" BYTEA,
    "zaehlstelle_id" character varying(36) NOT NULL,
    CONSTRAINT "zaehlstelleimage_pkey" PRIMARY KEY ("id")
);

