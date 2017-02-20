package com.youzan.common.component.sequence;

/**
 * @author: clong
 * @date: 2016-11-23
 */
public interface IdGenerator {

  /**
   * 生成id
   *
   * @param version    版本号
   * @param shardingId 分片id
   */
  long nextId(int version, int shardingId);


}