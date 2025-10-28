# Sample Data (Parts Domain)

- Status: Draft
- Date: 2025-10-28

This folder provides ready-to-import CSV datasets for the Parts domain. The data satisfies the requirement:
- Categories: ≥ 4 (we provide 5)
- Parts: ≥ 20 (we provide 24)
- Car Models: ≥ 20 (Hyundai/Kia representative models, no trims/years)
- Part–CarModel mappings: ≥ 20 (we provide 66), no duplicate (partId,carModelId)

## Files
- part_categories.csv: id,name,description,enabled,createdAt,updatedAt
- parts.csv: id,code,name,price,categoryId,imageUrl,enabled,createdAt,updatedAt
- car_models.csv: id,name,enabled,createdAt,updatedAt
- part_car_models.csv: id,partId,carModelId,note,enabled,createdAt,updatedAt

All timestamps are ISO8601 UTC strings (e.g., 2025-10-28T00:00:00Z).

## Import Order
1. part_categories.csv  (FK target for parts.categoryId)
2. parts.csv            (FK source for mappings.partId)
3. car_models.csv       (FK target for mappings.carModelId)
4. part_car_models.csv  (composite uniqueness on partId+carModelId implied by docs)

## PostgreSQL (local) examples
```sql
\copy part_category(id,name,description,enabled,created_at,updated_at)
FROM 'docs/sample-data/part_categories.csv' WITH (FORMAT csv, HEADER true);

\copy part(id,code,name,price,category_id,image_url,enabled,created_at,updated_at)
FROM 'docs/sample-data/parts.csv' WITH (FORMAT csv, HEADER true);

\copy car_model(id,name,enabled,created_at,updated_at)
FROM 'docs/sample-data/car_models.csv' WITH (FORMAT csv, HEADER true);

\copy part_car_model(id,part_id,car_model_id,note,enabled,created_at,updated_at)
FROM 'docs/sample-data/part_car_models.csv' WITH (FORMAT csv, HEADER true);
```
Notes:
- Adjust table/column names to your actual DDL if they differ from JPA defaults.
- If you rely on identity/auto-increment, ensure sequences are set ≥ max(id) after import.

## H2 (local) examples
```sql
-- H2 does not support \copy; use RUNSCRIPT or a client tool.
-- Sample approach in JDBC URL: INIT=\'RUNSCRIPT FROM \'docs/sample-data/import-h2.sql\'\'
```

## MySQL examples
```sql
LOAD DATA LOCAL INFILE 'docs/sample-data/part_categories.csv'
INTO TABLE part_category
FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 LINES
(id,name,description,enabled,created_at,updated_at);

LOAD DATA LOCAL INFILE 'docs/sample-data/parts.csv'
INTO TABLE part
FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 LINES
(id,code,name,price,category_id,image_url,enabled,created_at,updated_at);

LOAD DATA LOCAL INFILE 'docs/sample-data/car_models.csv'
INTO TABLE car_model
FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 LINES
(id,name,enabled,created_at,updated_at);

LOAD DATA LOCAL INFILE 'docs/sample-data/part_car_models.csv'
INTO TABLE part_car_model
FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 LINES
(id,part_id,car_model_id,note,enabled,created_at,updated_at);
```

## Integrity & Constraints Checklist
- part.code is unique (UQ_part_code)
- car_model.name is unique (UQ_carmodel_name)
- (part_id,car_model_id) has a unique constraint (UQ_part_car_model)
- FKs: parts.category_id exists; mappings.part_id and mappings.car_model_id exist

## What’s Included
- 5 categories (Filter/Brake/Engine/Electrical/Suspension)
- 24 parts with realistic pricing and images
- 20 car models (Hyundai/Kia representatives): Avante, Sonata, Grandeur, Tucson, Santa Fe, Kona, Venue, Ioniq 5, Palisade, Casper, K3, K5, K7, K8, Sportage, Sorento, Carnival, Seltos, Telluride, Ray
- 66 part–car model mappings with simple notes and enabled=true

## After Import: Quick Smoke Checks
- GET /api/v1/parts → should list 20+ items with pagination
- Optional: Verify a few category names appear in PartSummaryResponse.category.name
- If Part deletion guard uses PCM counts, deleting a mapped Part should return 409

## Defered (Next PR)
- Receiving/Shipping sample datasets
- Optional CommandLineRunner-based loader (profile gated)
- Swagger sample examples referencing these datasets
