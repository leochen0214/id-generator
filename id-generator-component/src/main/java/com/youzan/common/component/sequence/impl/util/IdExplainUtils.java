package com.youzan.common.component.sequence.impl.util;

import com.youzan.common.component.sequence.impl.DefaultExplainResult;
import com.youzan.common.component.sequence.impl.IdDistribution;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author: clong
 * @date: 2017-02-20
 */
public interface IdExplainUtils {

  static DefaultExplainResult explain(long id, IdDistribution idDistribution) {
    long version = extractPart(id, idDistribution.getVersionBits(), idDistribution.getVersionShift());
    long seconds = extractPart(id, idDistribution.getTimestampBits(), idDistribution.getTimestampShift());
    long shardingId = extractPart(id, idDistribution.getShardingBits(), idDistribution.getShardingShift());
    long sequence = extractPart(id, idDistribution.getSequenceBits(), idDistribution.getSequenceShift());

    Instant instant = Instant.ofEpochSecond(seconds + idDistribution.getCustomEpoch());
    ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());

    DefaultExplainResult defaultExplainResult = new DefaultExplainResult();
    defaultExplainResult.setVersion(version);
    defaultExplainResult.setSeconds(seconds);
    defaultExplainResult.setTimestamp(zonedDateTime.toLocalDateTime().toString());
    defaultExplainResult.setShardingId(shardingId);
    defaultExplainResult.setSequence(sequence);

    return defaultExplainResult;
  }


  static long extractPart(long id, int length, int idx) {
    long tmp1 = (1 << length) - 1;
    long tmp2 = tmp1 << idx;
    long tmp3 = id & tmp2;
    return tmp3 >> idx;
  }


}
