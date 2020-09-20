CREATE TABLE IF NOT EXISTS `springcloud`.`coupon_template` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `available` boolean NOT NULL DEFAULT false COMMENT 'availability',
  `expired` boolean NOT NULL DEFAULT false COMMENT 'expiration',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT 'name',
  `logo` varchar(256) NOT NULL DEFAULT '' COMMENT 'logo path',
  `intro` varchar(256) NOT NULL DEFAULT '' COMMENT 'description',
  `category` varchar(64) NOT NULL DEFAULT '' COMMENT 'category',
  `product_line` int(11) NOT NULL DEFAULT '0' COMMENT 'product line',
  `coupon_count` int(11) NOT NULL DEFAULT '0' COMMENT 'total number',
  `create_time` datetime NOT NULL DEFAULT '0000-01-01 00:00:00' COMMENT 'created time',
  `user_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'created by',
  `template_key` varchar(128) NOT NULL DEFAULT '' COMMENT 'code',
  `target` int(11) NOT NULL DEFAULT '0' COMMENT 'client',
  `rule` varchar(1024) NOT NULL DEFAULT '' COMMENT 'json rules',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_user_id` (`user_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COMMENT='coupon template';

-- truncate coupon_template;
