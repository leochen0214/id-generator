package com.youzan.common.component.sequence;


import com.youzan.common.component.sequence.exception.SequenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author: clong
 * @date: 2016-11-24
 */
public class RedisResponse {

  private static final Logger logger = LoggerFactory.getLogger(RedisResponse.class);

  private long startSequence;
  private long endSequence;
  private long seconds;


  private final LinkedBlockingQueue<Long> queue;


  public RedisResponse(List<Long> redisLuaResult) {
    if (redisLuaResult == null || redisLuaResult.size() != 3) {
      throw new RuntimeException("redis lua脚本执行返回结果异常");
    }

    this.startSequence = redisLuaResult.get(0);
    this.endSequence = redisLuaResult.get(1);
    this.seconds = redisLuaResult.get(2);
    queue = new LinkedBlockingQueue<>(size());
    queue.addAll(sequences());
  }

  public long takeOne() {
    try {
      return queue.take();
    } catch (InterruptedException e) {
      logger.error("obtain sequence value occur error", e);
      Thread.currentThread().interrupt();
      throw new SequenceException("obtain sequence value occur error", e);
    }


  }


  public boolean currentBatchSequenceAreUsed() {
    return queue.isEmpty();
  }


  private int size() {
    return (int) (endSequence - startSequence + 1);
  }

  private List<Long> sequences() {
    List<Long> sequences = new ArrayList<>(size());
    for (long i = startSequence; i <= endSequence; i++) {
      sequences.add(i);
    }

    return sequences;
  }


  public long getStartSequence() {
    return startSequence;
  }

  public long getEndSequence() {
    return endSequence;
  }

  public long getSeconds() {
    return seconds;
  }


  @Override
  public String toString() {
    return "startSequence=" + startSequence +
           ", endSequence=" + endSequence +
           ", seconds=" + seconds;
  }


}
