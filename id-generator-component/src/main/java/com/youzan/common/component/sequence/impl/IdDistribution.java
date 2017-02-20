package com.youzan.common.component.sequence.impl;

/**
 * @author: clong
 * @date: 2017-02-19
 */
public interface IdDistribution {

  int getVersionBits();

  int getTimestampBits();

  int getShardingBits();

  int getSequenceBits();

  int getShardingShift();

  int getTimestampShift();

  int getVersionShift();

  int getSequenceShift();

  long getCustomEpoch();

  long id(long version, long seconds, long shardingId, long sequence);


  /**
   * version 最大取值范围
   *
   * @return
   */
  default int getMaxVersion(){
    return getMaxInt(getVersionBits());
  }

  /**
   * 分片id 最大取值范围
   * @return
   */
  default int getMaxSharding(){
    return getMaxInt(getShardingBits());
  }


  /**
   * 每秒可生成sequence最大数量
   * @return
   */
  default long getMaxSequence(){
    return getMaxLong(getSequenceBits());
  }


   static int getMaxInt(int bits){
    return ~(-1 << bits);
  }

   static long getMaxLong(int bits){
    return ~(-1L << bits);
  }



}
