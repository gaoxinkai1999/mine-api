-- 添加商品批次管理标识
ALTER TABLE product 
ADD COLUMN is_batch_managed TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否启用批次管理（0-否，1-是）';

-- 创建批次表
CREATE TABLE batch (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL COMMENT '关联商品',
    batch_number VARCHAR(50) NOT NULL COMMENT '批次号（唯一）',
    production_date DATE COMMENT '生产日期',
    expiration_date DATE COMMENT '过期日期',
    purchase_detail_id INT COMMENT '关联采购明细（记录批次来源）',
    cost_price DECIMAL(10,2) COMMENT '批次成本价',
    status TINYINT(1) DEFAULT 1 COMMENT '批次状态（1-正常，0-禁用）',
    remark VARCHAR(200) COMMENT '批次备注',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_batch_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_batch_purchase_detail FOREIGN KEY (purchase_detail_id) REFERENCES purchase_detail(id),
    UNIQUE INDEX idx_batch_number (batch_number)
) COMMENT '商品批次信息表';

-- 创建库存表
CREATE TABLE inventory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    batch_id INT COMMENT '允许为 NULL（非批次商品）',
    warehouse_id INT COMMENT '仓库ID（可选扩展）',
    quantity INT NOT NULL DEFAULT 0 COMMENT '当前库存数量',
    available_quantity INT NOT NULL DEFAULT 0 COMMENT '可用库存',
    locked_quantity INT NOT NULL DEFAULT 0 COMMENT '锁定库存',
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_inventory_batch FOREIGN KEY (batch_id) REFERENCES batch(id),
    UNIQUE INDEX idx_unique_inventory (product_id, warehouse_id, batch_id),
    INDEX idx_inventory_query (product_id, warehouse_id, quantity)
) COMMENT '商品库存表';

-- 创建销售批次明细表
CREATE TABLE sale_batch_detail (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_detail_id INT NOT NULL COMMENT '关联订单明细',
    batch_id INT NOT NULL COMMENT '关联批次',
    quantity INT NOT NULL COMMENT '从该批次扣减的数量',
    unit_price DECIMAL(10,2) COMMENT '该批次商品的销售单价',
    CONSTRAINT fk_sale_detail_order FOREIGN KEY (order_detail_id) REFERENCES order_detail(id),
    CONSTRAINT fk_sale_detail_batch FOREIGN KEY (batch_id) REFERENCES batch(id)
) COMMENT '销售批次明细表';

-- 修改库存事务表，添加批次关联
ALTER TABLE inventory_transactions 
ADD COLUMN batch_id INT COMMENT '关联批次（仅批次商品需要）',
ADD CONSTRAINT fk_transaction_batch FOREIGN KEY (batch_id) REFERENCES batch(id); 