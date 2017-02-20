package com.youzan.common.component.sequence.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author: clong
 * @date: 2017-02-18
 *
 * ----------   --------------  -------------------  ------------------  -----------------
 * Sign(1bit)     Version(3bit)   Time(29bit)         Sharding(12bit)     Sequence(19bit) ----------
 * --------------  -------------------  ------------------  -----------------
 */

public class DefaultIdDistribution implements IdDistribution {

  private static final int VERSION_BITS = 3;
  private static final int TIMESTAMP_BITS = 29;
  private static final int SHARDING_BITS = 12;
  private static final int SEQUENCE_BITS = 19;


  private static final int SHARDING_SHIFT = SEQUENCE_BITS;
  private static final int TIMESTAMP_SHIFT = SEQUENCE_BITS + SHARDING_BITS;
  private static final int VERSION_SHIFT = TIMESTAMP_SHIFT + TIMESTAMP_BITS;

  private static final long CUSTOM_EPOCH = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toEpochSecond();


  @Override
  public int getVersionBits() {
    return VERSION_BITS;
  }

  @Override
  public int getTimestampBits() {
    return TIMESTAMP_BITS;
  }

  @Override
  public int getShardingBits() {
    return SHARDING_BITS;
  }

  @Override
  public int getSequenceBits() {
    return SEQUENCE_BITS;
  }

  @Override
  public int getShardingShift() {
    return SHARDING_SHIFT;
  }

  @Override
  public int getTimestampShift() {
    return TIMESTAMP_SHIFT;
  }

  @Override
  public int getVersionShift() {
    return VERSION_SHIFT;
  }

  @Override
  public int getSequenceShift() {
    return 0;
  }

  @Override
  public long getCustomEpoch() {
    return CUSTOM_EPOCH;
  }

  @Override
  public long id(long version, long seconds, long shardingId, long sequence) {
    long shiftedVersion = version << VERSION_SHIFT;
    long shiftedTimestamp = (seconds - getCustomEpoch()) << TIMESTAMP_SHIFT;
    long shiftedSharding = shardingId << SHARDING_SHIFT;

    return shiftedVersion | shiftedTimestamp | shiftedSharding | sequence;
  }


}
