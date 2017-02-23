package com.youzan.common.component.sequence.impl;


import com.youzan.common.component.sequence.IdGenerator;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.script.DigestUtils;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * @author: clong
 * @date: 2016-11-24
 */
public class IdGeneratorRedisScript implements RedisScript<List<Long>> {

  private static final Logger logger = LoggerFactory.getLogger(IdGeneratorRedisScript.class);


  private static final String LUA_SCRIPT_RESOURCE_PATH = "/id-generator.lua";
  private static String luaScriptContent;
  private static String luaScriptSha;

  static {
    try {
      luaScriptContent = IOUtils.toString(IdGenerator.class.getResourceAsStream(LUA_SCRIPT_RESOURCE_PATH), "utf-8");
      luaScriptSha = DigestUtils.sha1DigestAsHex(luaScriptContent);
      logger.info("luaScriptSha={}", luaScriptSha);
    } catch (Exception e) {
      logger.error("read lua script occur error", e);
    }
  }


  @Override
  public String getSha1() {
    return luaScriptSha;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class getResultType() {
    return List.class;
  }

  @Override
  public String getScriptAsString() {
    return luaScriptContent;
  }


}
