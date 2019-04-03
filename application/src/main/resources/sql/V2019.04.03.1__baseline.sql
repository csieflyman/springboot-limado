CREATE TABLE party (
    id VARCHAR(36) NOT NULL,
    version BIGINT NOT NULL,
    `identity` VARCHAR(30) NOT NULL,
    type VARCHAR(20) NOT NULL,
    name VARCHAR(30) NOT NULL,
    email VARCHAR(80) NULL,
    enabled BIT(1) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX identity_idx (`identity``),
    INDEX type_idx (type),
    INDEX name_idx (name),
    INDEX enabled_idx (enabled),
    UNIQUE type_identity_idx (type, identity)
);

CREATE TABLE party_rel (
    parent_id VARCHAR(36) NOT NULL,
    child_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (parent, children),
    INDEX parent_id_idx (parent_id),
    INDEX child_id_idx (child_id),
    CONSTRAINT FK_party_rel_parent FOREIGN KEY (parent_id) REFERENCES party (id),
    CONSTRAINT FK_party_rel_child FOREIGN KEY (child_id) REFERENCES party (id)
);

CREATE TABLE dag_edge (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    entry_edge_id BIGINT, direct_edge_id BIGINT,
    exit_edge_id BIGINT, start_vertex_id VARCHAR(36) NOT NULL,
    end_vertex_id VARCHAR(36) NOT NULL,
    hops INT NOT NULL,
    dag_id VARCHAR(150) NOT NULL,
    PRIMARY KEY (id),
    INDEX entry_edge_id_idx (entry_edge_id),
    INDEX direct_edge_id_idx (direct_edge_id),
    INDEX exit_edge_id_idx (exit_edge_id),
    INDEX start_vertex_id_idx (start_vertex_id),
    INDEX end_vertex_id_idx (end_vertex_id),
    INDEX hops_idx (hops),
    CONSTRAINT FK_dag_edge_start_vertex_id FOREIGN KEY (start_vertex_id) REFERENCES party (id),
    CONSTRAINT FK_dag_edge_end_vertex_id FOREIGN KEY (end_vertex_id) REFERENCES party (id)
);

CREATE TABLE interval_tree (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    node_id VARCHAR(36) NOT NULL,
    low INT NOT NULL,
    high INT NOT NULL,
    tree_id VARCHAR(50) NOT NULL,
    tree_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    INDEX node_id_idx(node_id),
    INDEX low_idx(low),
    INDEX high_idx(high),
    INDEX tree_id_idx(tree_id),
    INDEX tree_type_idx(tree_type),
    UNIQUE unique_node_type_idx (node_id, tree_type),
    CONSTRAINT FK_interval_tree_node_id FOREIGN KEY (node_id) REFERENCES party (id)
);

CREATE TABLE `auth_log` (
    `id` int unsigned NOT NULL AUTO_INCREMENT,
    `account` varchar(30) NOT NULL,
    `occur_at` datetime NOT NULL,
     success BIT(1) NOT NULL,
    `error_msg` mediumtext,
    `ip` varchar(45),
    PRIMARY KEY (`id`),
    KEY `auth_log_account` (`account`),
    KEY `auth_log_occur_at` (`occur_at`),
    KEY `auth_log_success` (`success`)
);

CREATE TABLE `error_log` (
    `id` int unsigned NOT NULL AUTO_INCREMENT,
    `identity` varchar(36),
    `api` varchar(255) NOT NULL,
    `occur_at` datetime NOT NULL,
    `error_msg` mediumtext,
    `body` mediumtext,
    PRIMARY KEY (`id`),
    KEY `error_log_identity` (`identity`),
    KEY `error_log_api` (`api`),
    KEY `error_log_occur_at` (`occur_at`)
);

CREATE TABLE `request_log` (
    `id` int unsigned NOT NULL AUTO_INCREMENT,
    `identity` varchar(36),
    `api` varchar(255),
    `req_time` datetime NOT NULL,
    `rsp_time` datetime,
    `rsp_status` char(3),
    `req_body` mediumtext,
    `rsp_body` mediumtext,
    `ip` varchar(45),
    PRIMARY KEY (`id`),
    KEY `request_log_identity` (`identity`),
    KEY `request_log_api` (`api`),
    KEY `request_log_req_time` (`req_time`)
);