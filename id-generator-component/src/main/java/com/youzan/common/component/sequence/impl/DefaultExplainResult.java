package com.youzan.common.component.sequence.impl;

import lombok.Data;

/**
 * @author: clong
 * @date: 2017-02-20
 */
@Data
public class DefaultExplainResult {

  private long version;
  private long seconds;
  private String timestamp;
  private long shardingId;
  private long sequence;


}
