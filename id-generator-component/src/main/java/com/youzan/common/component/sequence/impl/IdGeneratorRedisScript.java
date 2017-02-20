package com.youzan.common.component.sequence.impl;


import com.youzan.common.component.sequence.IdGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.script.DigestUtils;
import org.springframework.data.redis.core.script.RedisScript;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
      URL url = IdGenerator.class.getResource(LUA_SCRIPT_RESOURCE_PATH);
      luaScriptContent = new String(Files.readAllBytes(urlToPath(url)), "UTF-8");
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


  private static Path urlToPath(URL resource) throws IOException, URISyntaxException {

    Objects.requireNonNull(resource, "Resource URL cannot be null");
    URI uri = resource.toURI();

    String scheme = uri.getScheme();
    if (scheme.equals("file")) {
      return Paths.get(uri);
    }

    if (!scheme.equals("jar")) {
      throw new IllegalArgumentException("Cannot convert to Path: " + uri);
    }

    String s = uri.toString();
    int separator = s.indexOf("!/");
    String entryName = s.substring(separator + 2);
    URI fileURI = URI.create(s.substring(0, separator));

    FileSystem fs = FileSystems.newFileSystem(fileURI, Collections.emptyMap());
    return fs.getPath(entryName);
  }


  public static void main(String[] args) {
    RedisScript<List<Long>> r1 = new IdGeneratorRedisScript();
    RedisScript<List<Long>> r2 = new IdGeneratorRedisScript();
    System.out.println(r1.getSha1());
    System.out.println(r2.getSha1());

  }
}
