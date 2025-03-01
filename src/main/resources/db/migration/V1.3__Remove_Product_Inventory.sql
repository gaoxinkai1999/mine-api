-- 1. 为非批次管理商品创建库存记录
INSERT INTO inventory (product_id, batch_id, quantity)
SELECT 
    id as product_id,
    NULL as batch_id,
    inventory as quantity
FROM product
WHERE is_batch_managed = false AND inventory > 0;

-- 2. 为批次管理商品创建库存记录
-- 注意：由于批次管理商品的库存应该按批次记录，这里我们将旧的总库存添加到最新的批次中
INSERT INTO inventory (product_id, batch_id, quantity)
SELECT 
    p.id as product_id,
    b.id as batch_id,
    p.inventory as quantity
FROM product p
INNER JOIN (
    SELECT product_id, MAX(id) as id
    FROM batch
    GROUP BY product_id
) latest_batch ON latest_batch.product_id = p.id
INNER JOIN batch b ON b.id = latest_batch.id
WHERE p.is_batch_managed = true AND p.inventory > 0;

-- 3. 删除 Product 表中的 inventory 列
ALTER TABLE product DROP COLUMN inventory; 