-- 创建 users 表
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) NULL COMMENT '手机号',
    `role_code` VARCHAR(50) NULL COMMENT '角色编码',
    `name` VARCHAR(50) NULL COMMENT '姓名',
    `sex` VARCHAR(10) NULL COMMENT '性别',
    `id_card` VARCHAR(20) NULL COMMENT '身份证号',
    `avatar` VARCHAR(255) NULL COMMENT '头像URL',
    `status` INT NULL DEFAULT 1 COMMENT '状态(0:禁用,1:正常)',
    `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_username` (`username`),
    UNIQUE INDEX `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员用户
INSERT INTO `users` (`username`, `password`, `email`, `role_code`, `name`, `status`)
VALUES ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin@example.com', 'ADMIN', '系统管理员', 1);