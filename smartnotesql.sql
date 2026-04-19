-- 告诉 MySQL 使用这个数据库
USE `smart_note`; 

-- 1. 彻底删除旧表
DROP TABLE IF EXISTS `user`;

-- 2. 创建包含 nickname 的新表
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(255) NOT NULL COMMENT '登录账号',
  `password` varchar(255) NOT NULL COMMENT '登录密码',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(255) DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(255) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `motto` varchar(255) DEFAULT NULL COMMENT '个性签名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 插入测试账号
INSERT INTO `user` (`username`, `password`, `nickname`) 
VALUES ('test_user', '123456', '成功啦');