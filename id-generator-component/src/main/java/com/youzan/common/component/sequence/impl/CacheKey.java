package com.youzan.common.component.sequence.impl;

/**
 * 缓存的key值类
 *
 * Created by xiajun on 17/8/2.
 */
public class CacheKey {

  private String namespaceOfCurrentSeconds;

  private int version;

  private int shardingId;

  private int size;

  public CacheKey(String namespaceOfCurrentSeconds, int version, int shardingId, int size) {
    this.namespaceOfCurrentSeconds = namespaceOfCurrentSeconds;
    this.version = version;
    this.shardingId = shardingId;
    this.size = size;
  }

  public String getNamespaceOfCurrentSeconds() {
    return namespaceOfCurrentSeconds;
  }

  public void setNamespaceOfCurrentSeconds(String namespaceOfCurrentSeconds) {
    this.namespaceOfCurrentSeconds = namespaceOfCurrentSeconds;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public int getShardingId() {
    return shardingId;
  }

  public void setShardingId(int shardingId) {
    this.shardingId = shardingId;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }
}
