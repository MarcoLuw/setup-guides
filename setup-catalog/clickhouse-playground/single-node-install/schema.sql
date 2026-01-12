-- DATABASE: default
-- CREATE DATABASE default;


-- DATABASE: indexer
CREATE DATABASE IF NOT EXISTS indexer;


-- TABLE: block_timestamp
CREATE TABLE indexer.block_timestamp
(
    `block_number` UInt64,
    `timestamp` UInt64
)
ENGINE = ReplacingMergeTree
PRIMARY KEY block_number
ORDER BY block_number
SETTINGS index_granularity = 8192;

-- TABLE: decoded_events
CREATE TABLE indexer.decoded_events
(
    `block_number` UInt32,
    `transaction_hash` String,
    `contract_address` String,
    `log_index` UInt32,
    `event_hash` String,
    `event_name` String,
    `event_params` Array(Tuple(
        param_name String,
        param_type String,
        param_internal_type Nullable(String),
        param_value String,
        param_indexed Bool))
)
ENGINE = ReplacingMergeTree
PRIMARY KEY (block_number, transaction_hash, log_index, contract_address, event_hash, event_name)
ORDER BY (block_number, transaction_hash, log_index, contract_address, event_hash, event_name)
SETTINGS index_granularity = 8192;


-- TABLE: decoded_slot_states
CREATE TABLE indexer.decoded_slot_states
(
    `block_number` UInt32,
    `contract_address` String,
    `variable` String,
    `from_value` Nullable(String),
    `to_value` Nullable(String)
)
ENGINE = ReplacingMergeTree
PRIMARY KEY (block_number, contract_address, variable)
ORDER BY (block_number, contract_address, variable)
SETTINGS index_granularity = 8192;


-- TABLE: erc20_metadata
CREATE TABLE indexer.erc20_metadata
(
    `contract_address` String,
    `symbol` String,
    `decimals` Int16,
    `name` String,
    `last_seen_block` Int64
)
ENGINE = ReplacingMergeTree(last_seen_block)
PRIMARY KEY contract_address
ORDER BY contract_address
SETTINGS index_granularity = 8192;


-- MATERIALIZED VIEW: univ3_new_pool
CREATE MATERIALIZED VIEW indexer.univ3_new_pool
(
    `block_number` UInt32,
    `transaction_hash` String,
    `token0` String,
    `token1` String,
    `fee` String,
    `tick_spacing` String,
    `pool_address` String
)
ENGINE = MergeTree
ORDER BY block_number
SETTINGS index_granularity = 8192
AS SELECT
    block_number,
    transaction_hash,
    tupleElement(arrayElement(arrayFilter(x -> (x.param_name = 'token0'), event_params), 1), 'param_value') AS token0,
    tupleElement(arrayElement(arrayFilter(x -> (x.param_name = 'token1'), event_params), 1), 'param_value') AS token1,
    tupleElement(arrayElement(arrayFilter(x -> (x.param_name = 'fee'), event_params), 1), 'param_value') AS fee,
    tupleElement(arrayElement(arrayFilter(x -> (x.param_name = 'tickSpacing'), event_params), 1), 'param_value') AS tick_spacing,
    tupleElement(arrayElement(arrayFilter(x -> (x.param_name = 'pool'), event_params), 1), 'param_value') AS pool_address
FROM indexer.decoded_events
WHERE event_name = 'PoolCreated';


-- MATERIALIZED VIEW: univ3_pools
CREATE MATERIALIZED VIEW indexer.univ3_pools
(
    `block_number` UInt32,
    `transaction_hash` String,
    `token0` String,
    `token1` String,
    `fee` String,
    `tick_spacing` String,
    `pool_address` String
)
ENGINE = ReplacingMergeTree
ORDER BY (block_number, pool_address)
SETTINGS index_granularity = 8192
AS SELECT
    block_number,
    transaction_hash,
    tupleElement(arrayElement(event_params, 1), 'param_value') AS token0,
    tupleElement(arrayElement(event_params, 2), 'param_value') AS token1,
    tupleElement(arrayElement(event_params, 3), 'param_value') AS fee,
    tupleElement(arrayElement(event_params, 4), 'param_value') AS tick_spacing,
    tupleElement(arrayElement(event_params, 5), 'param_value') AS pool_address
FROM indexer.decoded_events
WHERE (contract_address = '0x204faca1764b154221e35c0d20abb3c525710498') AND (event_name = 'PoolCreated');


-- MATERIALIZED VIEW: univ4_pools
CREATE MATERIALIZED VIEW indexer.univ4_pools
(
    `block_number` UInt32,
    `transaction_hash` String,
    `pool_id` String,
    `currency0` String,
    `currency1` String,
    `fee` String,
    `tick_spacing` String,
    `hooks` String
)
ENGINE = ReplacingMergeTree
ORDER BY (block_number, pool_id)
SETTINGS index_granularity = 8192
AS SELECT
    block_number,
    transaction_hash,
    tupleElement(arrayElement(event_params, 1), 'param_value') AS pool_id,
    tupleElement(arrayElement(event_params, 2), 'param_value') AS currency0,
    tupleElement(arrayElement(event_params, 3), 'param_value') AS currency1,
    tupleElement(arrayElement(event_params, 4), 'param_value') AS fee,
    tupleElement(arrayElement(event_params, 5), 'param_value') AS tick_spacing,
    tupleElement(arrayElement(event_params, 6), 'param_value') AS hooks
FROM indexer.decoded_events
WHERE (contract_address = '0x188d586ddcf52439676ca21a244753fa19f9ea8e') AND (event_name = 'Initialize');

