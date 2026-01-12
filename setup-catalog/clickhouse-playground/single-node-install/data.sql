-- Insert sample data for block_timestamp (1M rows)
INSERT INTO indexer.block_timestamp
SELECT 
    number AS block_number,
    1704067200 + (number * 12) AS timestamp  -- Starting from 2024-01-01, 12 seconds per block
FROM numbers(1000000);

-- Insert sample data for decoded_events (1M rows)
-- Mix of PoolCreated events for Uniswap v3 and v4
INSERT INTO indexer.decoded_events
SELECT 
    (number % 1000000) + 1 AS block_number,
    concat('0x', lower(hex(sipHash64(number, 'tx')))) AS transaction_hash,
    CASE 
        WHEN number % 3 = 0 THEN '0x204faca1764b154221e35c0d20abb3c525710498'  -- Uniswap V3 Factory
        WHEN number % 3 = 1 THEN '0x188d586ddcf52439676ca21a244753fa19f9ea8e'  -- Uniswap V4
        ELSE concat('0x', lower(hex(sipHash64(number, 'contract'))))
    END AS contract_address,
    (number % 100) AS log_index,
    CASE 
        WHEN number % 3 < 2 THEN '0x783cca1c0412dd0d695e784568c96da2e9c22ff989357a2e8b1d9b2b4e6b7118'  -- PoolCreated event hash
        ELSE '0x' || lower(hex(sipHash64(number, 'event')))
    END AS event_hash,
    CASE 
        WHEN number % 3 < 2 THEN 'PoolCreated'
        WHEN number % 5 = 0 THEN 'Transfer'
        ELSE 'Swap'
    END AS event_name,
    CASE 
        -- Uniswap V3 PoolCreated events
        WHEN number % 3 = 0 THEN [
            ('token0', 'address', 'address', concat('0x', lower(hex(sipHash64(number, 'token0')))), true),
            ('token1', 'address', 'address', concat('0x', lower(hex(sipHash64(number, 'token1')))), true),
            ('fee', 'uint24', 'uint24', toString((number % 4) * 500 + 500), false),  -- 500, 1000, 1500, 2000
            ('tickSpacing', 'int24', 'int24', toString((number % 4) * 10 + 10), false),  -- 10, 20, 30, 40
            ('pool', 'address', 'address', concat('0x', lower(hex(sipHash64(number, 'pool')))), false)
        ]
        -- Uniswap V4 Initialize events
        WHEN number % 3 = 1 THEN [
            ('id', 'bytes32', 'bytes32', concat('0x', lower(hex(sipHash64(number, 'poolid')))), true),
            ('currency0', 'address', 'address', concat('0x', lower(hex(sipHash64(number, 'curr0')))), false),
            ('currency1', 'address', 'address', concat('0x', lower(hex(sipHash64(number, 'curr1')))), false),
            ('fee', 'uint24', 'uint24', toString((number % 4) * 500 + 500), false),
            ('tickSpacing', 'int24', 'int24', toString((number % 4) * 10 + 10), false),
            ('hooks', 'address', 'address', concat('0x', lower(hex(sipHash64(number, 'hooks')))), false)
        ]
        -- Other events
        ELSE [
            ('from', 'address', 'address', concat('0x', lower(hex(sipHash64(number, 'from')))), true),
            ('to', 'address', 'address', concat('0x', lower(hex(sipHash64(number, 'to')))), true),
            ('value', 'uint256', 'uint256', toString(number * 1000000), false)
        ]
    END AS event_params
FROM numbers(1000000);

-- Insert sample data for decoded_slot_states (1M rows)
INSERT INTO indexer.decoded_slot_states
SELECT 
    (number % 1000000) + 1 AS block_number,
    concat('0x', lower(hex(sipHash64(number, 'contract')))) AS contract_address,
    CASE 
        WHEN number % 5 = 0 THEN 'balance'
        WHEN number % 5 = 1 THEN 'allowance'
        WHEN number % 5 = 2 THEN 'totalSupply'
        WHEN number % 5 = 3 THEN 'owner'
        ELSE 'liquidity'
    END AS variable,
    CASE 
        WHEN number % 10 = 0 THEN NULL
        ELSE toString(number * 1000)
    END AS from_value,
    toString((number + 1) * 1000) AS to_value
FROM numbers(1000000);

-- Insert sample data for erc20_metadata (10K unique tokens)
INSERT INTO indexer.erc20_metadata
SELECT 
    concat('0x', lower(hex(sipHash64(number, 'token')))) AS contract_address,
    CASE 
        WHEN number % 10 = 0 THEN 'USDC'
        WHEN number % 10 = 1 THEN 'USDT'
        WHEN number % 10 = 2 THEN 'WETH'
        WHEN number % 10 = 3 THEN 'DAI'
        WHEN number % 10 = 4 THEN 'WBTC'
        ELSE concat('TKN', toString(number))
    END AS symbol,
    CASE 
        WHEN number % 10 IN (0, 1, 3) THEN 6   -- USDC, USDT, DAI-like
        WHEN number % 10 IN (2, 4) THEN 18     -- WETH, WBTC-like
        ELSE (number % 18) + 1
    END AS decimals,
    CASE 
        WHEN number % 10 = 0 THEN 'USD Coin'
        WHEN number % 10 = 1 THEN 'Tether USD'
        WHEN number % 10 = 2 THEN 'Wrapped Ether'
        WHEN number % 10 = 3 THEN 'Dai Stablecoin'
        WHEN number % 10 = 4 THEN 'Wrapped Bitcoin'
        ELSE concat('Token ', toString(number))
    END AS name,
    toInt64((number % 1000000) + 1) AS last_seen_block
FROM numbers(10000);

-- Verify data was inserted
SELECT 'block_timestamp' AS table_name, count() AS row_count FROM indexer.block_timestamp
UNION ALL
SELECT 'decoded_events', count() FROM indexer.decoded_events
UNION ALL
SELECT 'decoded_slot_states', count() FROM indexer.decoded_slot_states
UNION ALL
SELECT 'erc20_metadata', count() FROM indexer.erc20_metadata;

-- Check materialized views got populated
SELECT 'univ3_new_pool' AS mv_name, count() AS row_count FROM indexer.univ3_new_pool
UNION ALL
SELECT 'univ3_pools', count() FROM indexer.univ3_pools
UNION ALL
SELECT 'univ4_pools', count() FROM indexer.univ4_pools;

-- Sample queries to verify data looks reasonable
SELECT 'Sample from decoded_events:' AS info;
SELECT block_number, event_name, contract_address 
FROM indexer.decoded_events 
LIMIT 5;

SELECT 'Sample from univ3_pools:' AS info;
SELECT block_number, token0, token1, fee, pool_address 
FROM indexer.univ3_pools 
LIMIT 5;

SELECT 'Sample from erc20_metadata:' AS info;
SELECT contract_address, symbol, decimals, name 
FROM indexer.erc20_metadata 
LIMIT 5;