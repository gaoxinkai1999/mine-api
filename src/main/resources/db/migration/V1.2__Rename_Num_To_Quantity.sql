-- 修改order_detail表中的字段名
-- 将num字段改为quantity，保持与实体类和其他表的命名一致
-- 这个修改是为了统一命名规范，使得所有与数量相关的字段都使用quantity
ALTER TABLE order_detail CHANGE COLUMN num quantity INT NOT NULL COMMENT '购买数量'; 