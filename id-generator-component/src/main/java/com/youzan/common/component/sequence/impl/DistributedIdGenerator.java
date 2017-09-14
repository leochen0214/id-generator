package com.youzan.common.component.sequence.impl;

import com.youzan.common.component.sequence.IdGenerator;
import com.youzan.common.component.sequence.RedisResponse;
import com.youzan.common.component.sequence.exception.ExceedMaxSequenceException;
import com.youzan.common.component.sequence.exception.SequenceException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 分布式id实现，id有5段组成
 *
 * ----------   --------------  -------------------  ------------------  -----------------
 * Sign(1bit)     Version(3bit)   Time(29bit)         Sharding(12bit)     Sequence(19bit) ----------
 * --------------  -------------------  ------------------  -----------------
 *
 * @author: clong
 * @date: 2016-11-24
 */
public class DistributedIdGenerator implements IdGenerator {

  private static final Logger logger = LoggerFactory.getLogger(DistributedIdGenerator.class);

  //guava cache配置
  private static final long CACHE_SIZE = 10000; // 默认缓存数量
  private static final long CACHE_EXPIRE = 3; // 缓存过期时间
  private static final int DEFAULT_BATCH_SIZE = 1000;//按批次取每次取的数量
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private static RedisScript<List<Long>> redisScript = new IdGeneratorRedisScript();


  private final String namespace;
  private final int batchSize;
  private final StringRedisTemplate stringRedisTemplate;
  private final IdDistribution idDistribution;

  private Cache<String, RedisResponse> batchSequencesCache =
      CacheBuilder.newBuilder()
          .maximumSize(CACHE_SIZE) // maximum records can be cached
          .expireAfterAccess(CACHE_EXPIRE, TimeUnit.SECONDS) // cache expire time
          .build();


  public DistributedIdGenerator(RedisConnectionFactory connectionFactory,
                                String namespace) {
    this(connectionFactory, namespace, new DefaultIdDistribution());
  }

  public DistributedIdGenerator(RedisConnectionFactory connectionFactory,
                                String namespace,
                                IdDistribution idDistribution) {
    this(connectionFactory, namespace, idDistribution, DEFAULT_BATCH_SIZE);
  }

  public DistributedIdGenerator(RedisConnectionFactory connectionFactory,
                                String namespace,
                                IdDistribution idDistribution,
                                int batchSize) {
    this.stringRedisTemplate = new StringRedisTemplate(connectionFactory);
    this.namespace = namespace;
    this.idDistribution = idDistribution;
    this.batchSize = batchSize;
  }


  @Override
  public long nextId(int version, int shardingId) {
    RedisResponse currentBatch = getRedisResponse(version, shardingId);

    long sequence = currentBatch.takeOne();
    long seconds = currentBatch.getSeconds();
    return idDistribution.id(version, seconds, shardingId, sequence);
  }

  private RedisResponse getRedisResponse(int version, int shardingId) {
    String namespaceOfCurrentSeconds = namespace + Instant.now().getEpochSecond();
    RedisResponse currentBatch = batchSequencesCache.getIfPresent(namespaceOfCurrentSeconds);
    if (currentBatch == null || currentBatch.currentBatchSequenceAreUsed()) {
      currentBatch = nextBatchValue(version, shardingId, batchSize);
      batchSequencesCache.put(namespaceOfCurrentSeconds, currentBatch);//会覆盖已有的键值
    }
    return currentBatch;
  }


  @Override
  public String nextId() {
    RedisResponse currentBatch = getRedisResponse(0, 0);

    long sequence = currentBatch.takeOne();
    long seconds = currentBatch.getSeconds();

    String time = LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault())
        .format(FORMATTER);

    return time + sequence;
  }


  private RedisResponse nextBatchValue(int version, int shardingId, int size) {
    validateParam(version, shardingId);

    RedisResponse redisResponse = evalLuaScript(size);

    //检查是否超出sequence最大值
    checkWeatherExceedMaxSequenceValueInOneSeconds(redisResponse, size);

    logger.info("redisResponse={}", redisResponse.toString());
    return redisResponse;
  }


  private void checkWeatherExceedMaxSequenceValueInOneSeconds(RedisResponse redisResponse,
                                                              int size) {
    long startSequence = redisResponse.getStartSequence();
    long endSequence = redisResponse.getEndSequence();
    long totalAvailableIds = endSequence - startSequence;
    if (totalAvailableIds < (size - 1)) {
      throw new ExceedMaxSequenceException("获取id的数量少于指定的数量：" + size);
    }
  }


  private void validateParam(int version, int shardingId) {
    validateVersion(version);
    validateShardingId(shardingId);
  }

  private void validateVersion(int version) {
    int maxVersion = idDistribution.getMaxVersion();
    if (version < 0 || version > maxVersion) {
      throw new IllegalArgumentException("version must be in [0, " + maxVersion + "]");
    }
  }

  private void validateShardingId(int shardingId) {
    int maxSharding = idDistribution.getMaxSharding();
    if (shardingId < 0 || shardingId > maxSharding) {
      throw new IllegalArgumentException("shardingId must be in [0, " + maxSharding + "]");
    }
  }


  /**
   * 先加载lua脚本，然后使用evalsha命令执行
   */
  private RedisResponse evalLuaScript(int size) {
    String seconds = Instant.now().getEpochSecond() + "";
    String sequenceKey = namespace + seconds;
    List<String> args = new ArrayList<>(4);
    args.add(sequenceKey);
    args.add(seconds);
    args.add(String.valueOf(idDistribution.getMaxSequence()));
    args.add(String.valueOf(size));

    try {
      List<Long> result = stringRedisTemplate.execute(redisScript, args);
      return new RedisResponse(result);
    } catch (Exception e) {
      String msg = "obtain sequence from redis server occur error, sequenceKey=" + sequenceKey;
      logger.error(msg, e);
      throw new SequenceException(msg, e);
    }
  }


}