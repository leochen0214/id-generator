> 单机系统中生成一个唯一id很简单，利用数据库自身的特性就可以完成。但在分库分表分布式场景下，要声称一个唯一id就变得复杂了，所生成的全局的 unique ID 要满足以下需求：

- 唯一性
- 时间相关
- 大致有序
- 生成 ID 的速度有要求. 例如, 在一个高吞吐量的场景中, 需要每秒生成几万个 ID (Twitter 最新的峰值到达了 143,199 Tweets/s, 也就是 10万+/秒)
- 可反解,可制造
- 服务无单点

#### 唯一性
> 唯一性我们只要保障某个命名空间下唯一就行，不需要全局唯一，例如生成的订单id，用户id在各自的命名空间内都是唯一的，但彼此之间生成的id可能相同

#### 时间用来干嘛
> 我们知道时间是天然唯一的，永远向前走(不讨论穿越情况)因此很多设计都会利用这个特性，twitter的SnowFlake利用了，但对于一个8字节（共64位）的id而言，时间没有那么多（一万年太久，只争朝夕），如果精确到秒级别，10年要用到29位，30年要用到30位，剩下的位可以用来做其他事情。
之所以在8Byte 上捣鼓，因为8Byte 是一个Long，不管在处理器和编译器还是语言层面，都是可以更好地被处理。

#### 大致有序，有多大致
> 如果我们精确到秒级别，那么后一秒的id肯定比前一秒的id大，但是同一秒内可能后面取的id可能比前面小。所以这个大致体现在整个id只保证时间上的有序性，在秒级别上不再保证有序。

#### 生成id的速度
> 我们知道1秒生成1个id明显是不够的，所以我们有sequence位可用，19位sequence位可以每秒生成大约52w个ID，52w对于目前业务量来说是足够使用了

#### 可反解
> 一个 ID 生成之后，就会伴随着信息终身，排错分析的时候，我们需要查验。这时候一个可反解的 ID 可以帮上很多忙，从哪里来的，什么时候出生的,落在哪个数据库分片上等等， 跟身份证倒有点儿相通了，其实身份证就是一个典型的分布式 ID 生成器。


### 我们的做法
- 时间精确到秒，使用29位
- sequence位使用19位
- sharding位使用12位
- version位3位
- 符号位1位，保留

> 这样生成的id中自带分片信息，如果要通过订单id查询某个订单信息，只需要orderId一个参数即可，分片字段可通过id反解出来。

#### 实现细节
> 生成ID时，利用当前机器时间(秒数)+ namespace作为key去redis集群获取一批sequence区间，用来生成ID.我们知道机器间存在同步时间差，所以需要为key设置一个安全的过期时间，一般10分钟足够了，ntp同步间隔肯定小于这个过期时间。

> 利用redis lua脚本将一系列redis命令在都省略掉，直接在redis服务端执行

```
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

```

### quick start
```
  <dependency>
            <groupId>com.youzan</groupId>
            <artifactId>id-generator-component</artifactId>
             <version>1.0.1-RELEASE</version>
</dependency>
```

- 配置

```
 @Bean
  public IdGenerator orderIdGenerator(@Autowired RedisConnectionFactory connectionFactory){
    return new DistributedIdGenerator(connectionFactory, "tc_order");
  }
```
- 使用

```
 @Resource(name = "orderIdGenerator")
  IdGenerator orderIdGenerator;

```

#### 生成id与反解
curl -G http://localhost:8080/gen
```
orderIds=[1365715004573089793, 1365715004573089794, 1365715004573089795]
```

curl -G http://localhost:8080/explain/1365715004573089794

```
{
version: 1,
seconds: 99089695,
timestamp: "2017-02-20T20:54:55",
shardingId: 512,
sequence: 2
}

```

#### 参考文章
- https://github.com/twitter/snowflake
- http://engineering.intenthq.com/2015/03/icicle-distributed-id-generation-with-redis-lua/
- http://darktea.github.io/notes/2013/12/08/Unique-ID
- http://ericliang.info/what-kind-of-id-generator-we-need-in-business-systems/