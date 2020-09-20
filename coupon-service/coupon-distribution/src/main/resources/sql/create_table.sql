CREATE TABLE IF NOT EXISTS `springcloud`.`coupon` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `coupon_code` varchar(64) NOT NULL DEFAULT '' COMMENT 'coupon code',
  `user_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'user id',
  `template_id` int(11) NOT NULL DEFAULT '0' COMMENT 'template code',
  `assign_time` datetime NOT NULL DEFAULT '0000-01-01 00:00:00' COMMENT 'assign time',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT 'status',
  PRIMARY KEY (`id`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COMMENT='coupon';