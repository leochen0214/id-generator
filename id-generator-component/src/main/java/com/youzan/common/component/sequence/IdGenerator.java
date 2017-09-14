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


  /**
   * 时间+sequence组成
   * eg: 201709142059586, 201709142059587
   * @return
   */
  String nextId();

  default long nextIdValue(){
    return Long.parseLong(nextId());
  }

  default long nextValue() {
    return nextId(0, 0);
  }


}