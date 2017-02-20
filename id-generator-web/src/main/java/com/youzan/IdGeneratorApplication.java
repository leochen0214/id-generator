package com.youzan;

import com.youzan.common.component.sequence.IdGenerator;
import com.youzan.common.component.sequence.impl.DistributedIdGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author: clong
 * @date: 2017-02-20
 */
@SpringBootApplication
public class IdGeneratorApplication {

  public static void main(String[] args) {
    SpringApplication.run(IdGeneratorApplication.class, args);
  }


  @Bean
  public IdGenerator orderIdGenerator(@Autowired RedisConnectionFactory connectionFactory){
    return new DistributedIdGenerator(connectionFactory, "tc_order");
  }

}
