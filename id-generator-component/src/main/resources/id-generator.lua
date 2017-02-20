local sequence_key = KEYS[1]
local app_server_time = tonumber(KEYS[2])
local max_sequence = tonumber(KEYS[3])
local size = tonumber(KEYS[4])
local lock_key = 'lock-' .. sequence_key


if redis.call('EXISTS', lock_key) == 1 then
   redis.log(redis.LOG_WARNING, 'Cannot generate ID, waiting for lock to expire.')
   return redis.error_reply('Cannot generate ID, waiting for lock to expire.')
end

redis.pcall('EXPIRE', sequence_key, 600) -- 10min expire, ntp time gap should be less than 10min

--[[
Increment by a set number, this can
--]]
local end_sequence = redis.pcall('INCRBY', sequence_key, size)
local start_sequence = end_sequence - size + 1
if end_sequence >= max_sequence then
    redis.log(redis.LOG_WARNING, 'Rolling sequence back to the start, locking for 1s.')
    redis.pcall('PSETEX', lock_key, 1000, 'lock')
    end_sequence = max_sequence
end

return {
    start_sequence,
    end_sequence, -- Doesn't need conversion, the result of INCR or the variable set is always a number.
    app_server_time
}
