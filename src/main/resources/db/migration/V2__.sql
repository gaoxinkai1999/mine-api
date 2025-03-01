ALTER TABLE old.stock_in_detail
    DROP FOREIGN KEY stock_in_detail_product_id_fk;

ALTER TABLE old.stock_in_detail
    DROP FOREIGN KEY stock_in_detail_stock_in_id_fk;

CREATE TABLE old.batch
(
    id                 INT AUTO_INCREMENT       NOT NULL,
    product_id         INT                      NOT NULL COMMENT '关联商品',
    batch_number       VARCHAR(50)              NOT NULL COMMENT '批次号（唯一）',
    production_date    date                     NULL COMMENT '生产日期',
    expiration_date    date                     NULL COMMENT '过期日期',
    purchase_detail_id INT                      NULL COMMENT '关联采购明细（记录批次来源）',
    cost_price         DECIMAL(10, 2)           NULL COMMENT '批次成本价',
    status             TINYINT(1) DEFAULT 1     NULL COMMENT '批次状态（1-正常，0-禁用）',
    remark             VARCHAR(200)             NULL COMMENT '批次备注',
    created_time       datetime   DEFAULT NOW() NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) COMMENT ='商品批次信息表';

CREATE TABLE old.category
(
    id          INT AUTO_INCREMENT       NOT NULL,
    create_time datetime   DEFAULT NOW() NOT NULL,
    name        VARCHAR(255)             NOT NULL,
    is_del      TINYINT(1) DEFAULT 0     NOT NULL,
    sort        INT                      NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE old.inventory
(
    id         INT AUTO_INCREMENT NOT NULL,
    product_id INT                NOT NULL,
    batch_id   INT                NULL COMMENT '允许为 NULL（非批次商品）',
    quantity   INT DEFAULT 0      NOT NULL COMMENT '当前库存数量',
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) COMMENT ='商品库存表';

CREATE TABLE old.inventory_transactions
(
    id               INT AUTO_INCREMENT      NOT NULL,
    product_id       INT                     NOT NULL,
    quantity         INT                     NOT NULL,
    operation_type   ENUM ()                   NOT NULL,
    transaction_time timestamp DEFAULT NOW() NOT NULL,
    order_id         INT                     NULL,
    batch_id         INT                     NULL COMMENT '关联批次（仅批次商品需要）',
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE old.purchase
(
    id           INT AUTO_INCREMENT NOT NULL,
    create_time  datetime           NOT NULL,
    total_amount DECIMAL(10, 2)     NOT NULL,
    in_time      datetime           NULL,
    state        ENUM               NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE old.purchase_detail
(
    id           INT AUTO_INCREMENT NOT NULL,
    product_id   INT                NOT NULL,
    quantity     INT                NOT NULL,
    total_amount DECIMAL(10, 2)     NOT NULL,
    purchase_id  INT                NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE old.sale_batch_detail
(
    id              INT AUTO_INCREMENT NOT NULL,
    order_detail_id INT                NOT NULL COMMENT '关联订单明细',
    batch_id        INT                NOT NULL COMMENT '关联批次',
    quantity        INT                NOT NULL COMMENT '从该批次扣减的数量',
    unit_price      DECIMAL(10, 2)     NULL COMMENT '该批次商品的销售单价',
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) COMMENT ='销售批次明细表';

ALTER TABLE old.product
    ADD category_id INT NOT NULL;

ALTER TABLE old.product
    ADD is_batch_managed TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否启用批次管理（0-否，1-是）';

ALTER TABLE old.product
    ADD sort INT NULL;

ALTER TABLE old.order_detail
    ADD quantity INT NOT NULL COMMENT '购买数量';

ALTER TABLE old.batch
    ADD CONSTRAINT idx_batch_number UNIQUE (batch_number);

ALTER TABLE old.inventory
    ADD CONSTRAINT idx_unique_inventory UNIQUE (product_id, batch_id);

CREATE INDEX idx_inventory_query ON old.inventory (product_id, quantity);

ALTER TABLE old.batch
    ADD CONSTRAINT fk_batch_product FOREIGN KEY (product_id) REFERENCES old.product (id) ON DELETE NO ACTION;

CREATE INDEX fk_batch_product ON old.batch (product_id);

ALTER TABLE old.batch
    ADD CONSTRAINT fk_batch_purchase_detail FOREIGN KEY (purchase_detail_id) REFERENCES old.purchase_detail (id) ON DELETE NO ACTION;

CREATE INDEX fk_batch_purchase_detail ON old.batch (purchase_detail_id);

ALTER TABLE old.inventory
    ADD CONSTRAINT fk_inventory_batch FOREIGN KEY (batch_id) REFERENCES old.batch (id) ON DELETE NO ACTION;

CREATE INDEX fk_inventory_batch ON old.inventory (batch_id);

ALTER TABLE old.inventory
    ADD CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES old.product (id) ON DELETE NO ACTION;

ALTER TABLE old.sale_batch_detail
    ADD CONSTRAINT fk_sale_detail_batch FOREIGN KEY (batch_id) REFERENCES old.batch (id) ON DELETE NO ACTION;

CREATE INDEX fk_sale_detail_batch ON old.sale_batch_detail (batch_id);

ALTER TABLE old.sale_batch_detail
    ADD CONSTRAINT fk_sale_detail_order FOREIGN KEY (order_detail_id) REFERENCES old.order_detail (id) ON DELETE NO ACTION;

CREATE INDEX fk_sale_detail_order ON old.sale_batch_detail (order_detail_id);

ALTER TABLE old.inventory_transactions
    ADD CONSTRAINT fk_transaction_batch FOREIGN KEY (batch_id) REFERENCES old.batch (id) ON DELETE NO ACTION;

CREATE INDEX fk_transaction_batch ON old.inventory_transactions (batch_id);

ALTER TABLE old.inventory_transactions
    ADD CONSTRAINT inventory_transactions_ibfk_1 FOREIGN KEY (product_id) REFERENCES old.product (id) ON DELETE CASCADE;

CREATE INDEX product_id ON old.inventory_transactions (product_id);

ALTER TABLE old.inventory_transactions
    ADD CONSTRAINT inventory_transactions_ibfk_2 FOREIGN KEY (order_id) REFERENCES old.`order` (id) ON DELETE SET NULL;

CREATE INDEX order_id ON old.inventory_transactions (order_id);

ALTER TABLE old.product
    ADD CONSTRAINT product_category_id_fk FOREIGN KEY (category_id) REFERENCES old.category (id) ON DELETE NO ACTION;

CREATE INDEX product_category_id_fk ON old.product (category_id);

ALTER TABLE old.purchase_detail
    ADD CONSTRAINT purchase_detail_product_id_fk FOREIGN KEY (product_id) REFERENCES old.product (id) ON DELETE NO ACTION;

CREATE INDEX stock_in_detail_product_id_fk ON old.purchase_detail (product_id);

ALTER TABLE old.purchase_detail
    ADD CONSTRAINT purchase_detail_purchase_id_fk FOREIGN KEY (purchase_id) REFERENCES old.purchase (id) ON DELETE NO ACTION;

CREATE INDEX purchase_detail_purchase_id_fk ON old.purchase_detail (purchase_id);

DROP TABLE old.stock_in;

DROP TABLE old.stock_in_detail;

ALTER TABLE old.product
    DROP COLUMN discounted_price;

ALTER TABLE old.product
    DROP COLUMN `index`;

ALTER TABLE old.product
    DROP COLUMN inventory;

ALTER TABLE old.order_detail
    DROP COLUMN num;

ALTER TABLE old.`order`
    DROP COLUMN type;

ALTER TABLE old.shop
    MODIFY arrears DECIMAL(38, 2);