CREATE TABLE laengsverkehr (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    richtung character varying(255),
    strassenseite character varying(255),
    knotenarm integer,
    zaehlung character varying(36) NOT NULL
);

ALTER TABLE laengsverkehr OWNER TO dave;

ALTER TABLE laengsverkehr ADD CONSTRAINT laengsverkehr_pkey PRIMARY KEY (id);

ALTER TABLE laengsverkehr ADD CONSTRAINT fk_laengsverkehr_zaehlung FOREIGN KEY (zaehlung) REFERENCES zaehlung(id);

CREATE INDEX idx_laengsverkehr_zaehlung ON laengsverkehr(zaehlung);

-- Table for Querungsverkehr
CREATE TABLE querungsverkehr (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    richtung character varying(255),
    knotenarm integer,
    zaehlung character varying(36) NOT NULL
);

ALTER TABLE querungsverkehr OWNER TO dave;
ALTER TABLE querungsverkehr ADD CONSTRAINT querungsverkehr_pkey PRIMARY KEY (id);
ALTER TABLE querungsverkehr ADD CONSTRAINT fk_querungsverkehr_zaehlung FOREIGN KEY (zaehlung) REFERENCES zaehlung(id);
CREATE INDEX idx_querungsverkehr_zaehlung ON querungsverkehr(zaehlung);

