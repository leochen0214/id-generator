package com.youzan.common.component.sequence.exception;

/**
 * 超出sequence位最大值异常
 *
 * @author: clong
 * @date: 2016-11-18
 */
public class ExceedMaxSequenceException extends SequenceException {

  public ExceedMaxSequenceException(String message) {
    super(message);
  }

  public ExceedMaxSequenceException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExceedMaxSequenceException(Throwable cause) {
    super(cause);
  }
}
