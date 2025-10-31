# Sample Data (CSV)

This folder contains mock CSVs for local testing and demos. Each table has at least 5 rows and preserves basic relationships across notes, lines, parts, and inventory.

Important:
- Headers now match the provided DDL exactly (snake_case and column names as in DDL). Column order in these CSVs follows the DDL shared in this sprint.
- Timestamps:
  - `with time zone` columns use ISO‑8601 UTC (e.g., 2025-10-27T02:00:00Z).
  - `without time zone` columns use `YYYY-MM-DD HH:MM:SS`.
- IDs are chosen for readability; adjust if your DB uses sequences/UUIDs.

Provided files:
- part_category.csv — Seed categories used by parts (DDL-aligned headers)
- car_model.csv — Car models master (unique name)
- part.csv — Parts master data (enabled mix; category_id FK)
- part_car_model.csv — Mapping table between part and car model (unique part_id+car_model_id)
- inventory_onhand.csv — On‑hand quantities across two warehouses (1 and 2)
- receiving_note.csv — Receiving note headers (various statuses)
- receiving_note_line.csv — Receiving note lines; references `note_id` and `product_id`
- shipping_note.csv — Shipping note headers (various statuses)
- shipping_note_line.csv — Shipping note lines; references `note_id` and `product_id`

Entity mapping reference (current code → CSV headers):
- Parts
  - part_category → `PartCategoryEntity` → CSV: enabled, created_at, id, updated_at, name, description
  - part → `PartEntity` → CSV: enabled, price, category_id, created_at, id, updated_at, name, code, image_url
  - car_model → `CarModelEntity` → CSV: enabled, created_at, id, updated_at, name
  - part_car_model → `PartCarModelEntity` → CSV: enabled, car_model_id, created_at, id, part_id, updated_at, note
- Inventory
  - inventory_onhand → `InventoryOnHandEntity` → CSV: id, warehouseId, partId, onHandQty, lastUpdatedAt (unchanged in this step)
- Receiving
  - receiving_note → `ReceivingNoteEntity` → CSV: item_kinds_number, total_qty, completed_at, created_at, expected_receive_date, note_id, received_at, requested_at, updated_at, warehouse_id, inspector_dept, inspector_name, inspector_phone, receiving_no, remark, status, supplier_name
  - receiving_note_line → `ReceivingNoteLineEntity` → CSV: inspected_qty, issue_qty, ordered_qty, created_at, line_id, note_id, product_id, updated_at, product_code, product_img_url, product_lot, product_name, remark, status
- Shipping
  - shipping_note → `ShippingNoteEntity` → CSV: item_kinds_number, total_qty, completed_at, created_at, expected_ship_date, note_id, requested_at, shipped_at, updated_at, warehouse_id, assignee_dept, assignee_name, assignee_phone, customer_name, remark, shipping_no, status
  - shipping_note_line → `ShippingNoteLineEntity` → CSV: allocated_qty, ordered_qty, picked_qty, line_id, note_id, product_id, product_img_url, product_lot, product_name, product_serial, status

Notes on consistency:
- All `receiving_note_line.note_id` values exist in receiving_note.csv.
- All `shipping_note_line.note_id` values exist in shipping_note.csv.
- All line rows use `product_id` values present in part.csv.
- Status vs quantities are coherent (examples):
  - Receiving ACCEPTED lines have `issue_qty=0`; REJECTED lines have `issue_qty=ordered_qty-inspected_qty`.
  - Shipping READY lines have `picked_qty<=allocated_qty<=ordered_qty`; SHORTAGE has `picked_qty<allocated_qty`.
- Inventory numbers are plausible relative to shipping/receiving examples but not enforced.

Load order recommendation:
1) part_category.csv
2) car_model.csv
3) part.csv
4) receiving_note.csv
5) receiving_note_line.csv
6) shipping_note.csv
7) shipping_note_line.csv
8) part_car_model.csv

Note:
- Production DB column order may still differ; importers should map by column name if possible.
