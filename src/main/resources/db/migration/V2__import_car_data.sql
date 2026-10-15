CREATE TEMP TABLE raw_car_import (
	source_object_id TEXT,
	make TEXT,
	model_year TEXT,
	model_name TEXT,
	category TEXT
);

COPY raw_car_import (
	source_object_id,
	make,
	model_year,
	model_name,
	category
)
FROM '/data/cars.csv'
WITH (
	FORMAT CSV,
	HEADER TRUE
);

-- The source CSV contains one completely empty record.
DELETE FROM raw_car_import
WHERE COALESCE(BTRIM(source_object_id), '') = ''
	AND COALESCE(BTRIM(make), '') = ''
	AND COALESCE(BTRIM(model_year), '') = ''
	AND COALESCE(BTRIM(model_name), '') = ''
	AND COALESCE(BTRIM(category), '') = '';

-- Fail the migration instead of silently skipping malformed source records.
DO $$
BEGIN
	IF EXISTS (
		SELECT 1
		FROM raw_car_import
		WHERE NULLIF(BTRIM(make), '') IS NULL
			OR NULLIF(BTRIM(model_year), '') IS NULL
			OR NULLIF(BTRIM(model_name), '') IS NULL
			OR NULLIF(BTRIM(category), '') IS NULL
			OR BTRIM(model_year) !~ '^[0-9]{4}$'
	) THEN
		RAISE EXCEPTION 'Invalid record found in car CSV import';
	END IF;
END
$$;

-- 1. Manufacturers

INSERT INTO manufacturer (name)
SELECT DISTINCT BTRIM(make) AS name
FROM raw_car_import
ORDER BY name
ON CONFLICT (name) DO NOTHING;

-- 2. Models

INSERT INTO car_model (
	name,
	manufacturer_id
)
SELECT DISTINCT
	BTRIM(r.model_name),
	m.id
FROM raw_car_import r
JOIN manufacturer m
	ON m.name = BTRIM(r.make)
ORDER BY
	m.id,
	BTRIM(r.model_name)
ON CONFLICT (manufacturer_id, name) DO NOTHING;

-- 3. Categories

INSERT INTO category (name)
SELECT DISTINCT
	BTRIM(split_category.name) AS name
FROM raw_car_import r
CROSS JOIN LATERAL regexp_split_to_table(
	r.category,
	'\s*,\s*'
) AS split_category(name)
WHERE BTRIM(split_category.name) <> ''
ORDER BY name
ON CONFLICT (name) DO NOTHING;

-- 4. Cars

INSERT INTO car (
	year,
	model_id
)
SELECT DISTINCT
	BTRIM(r.model_year)::SMALLINT,
	cm.id
FROM raw_car_import r
JOIN manufacturer m
	ON m.name = BTRIM(r.make)
JOIN car_model cm
	ON cm.manufacturer_id = m.id
	AND cm.name = BTRIM(r.model_name)
ORDER BY
	cm.id,
	BTRIM(r.model_year)::SMALLINT
ON CONFLICT (model_id, year) DO NOTHING;

-- 5. Car-category relations

INSERT INTO car_category (
	car_id,
	category_id
)
SELECT DISTINCT
	c.id,
	cat.id
FROM raw_car_import r
JOIN manufacturer m
	ON m.name = BTRIM(r.make)
JOIN car_model cm
	ON cm.manufacturer_id = m.id
	AND cm.name = BTRIM(r.model_name)
JOIN car c
	ON c.model_id = cm.id
	AND c.year = BTRIM(r.model_year)::SMALLINT
CROSS JOIN LATERAL regexp_split_to_table(
	r.category,
	'\s*,\s*'
) AS split_category(name)
JOIN category cat
	ON cat.name = BTRIM(split_category.name)
WHERE BTRIM(split_category.name) <> ''
ON CONFLICT (car_id, category_id) DO NOTHING;

DROP TABLE raw_car_import;