package com.youzan;

import com.youzan.common.component.sequence.IdGenerator;
import com.youzan.common.component.sequence.impl.DefaultExplainResult;
import com.youzan.common.component.sequence.impl.DefaultIdDistribution;
import com.youzan.common.component.sequence.impl.util.IdExplainUtils;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

/**
 * @author: clong
 * @date: 2017-02-20
 */
@RestController
public class IdGeneratorTestRestController {

  @Resource(name = "orderIdGenerator")
  IdGenerator orderIdGenerator;


  @RequestMapping("/gen")
  public List<Long> gen(@RequestParam(required = false) Integer size) {
    if (size == null || size <= 0) {
      size = 5;
    }

    List<Long> orderIds = generateIds(orderIdGenerator, size);
//    DefaultIdDistribution defaultIdDistribution = new DefaultIdDistribution();

//    List<DefaultExplainResult> explainResults = orderIds.stream().map(id->IdExplainUtils.explain(id, defaultIdDistribution))
//        .collect(toList());

    return orderIds;
  }

  private List<Long> generateIds(IdGenerator idGenerator, int size) {
    List<Long> ids = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      ids.add(idGenerator.nextId(1, 512));
    }

    return ids;
  }


  @GetMapping("/explain/{id}")
  public DefaultExplainResult explain(@PathVariable long id) {
    return IdExplainUtils.explain(id, new DefaultIdDistribution());
  }


}
