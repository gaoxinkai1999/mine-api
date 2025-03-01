-- 移除 inventory 表中的 warehouse_id 列
ALTER TABLE inventory DROP COLUMN IF EXISTS warehouse_id; 