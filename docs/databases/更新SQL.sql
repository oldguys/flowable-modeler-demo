# 2020-1102

CREATE TABLE `test_task_action_log` (
  `id` bigint(20) NOT NULL,
  `task_id` varchar(64) NOT NULL,
  `task_name` varchar(255) DEFAULT NULL,
  `type` tinyint(4) NOT NULL,
  `action` varchar(255) NOT NULL,
  `comment_id` varchar(64) DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

