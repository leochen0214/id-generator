package com.youzan.common.component.sequence.exception;

/**
 * @author: clong
 * @date: 2016-11-17
 */
public class SequenceException extends RuntimeException {

  public SequenceException(String message) {
    super(message);
  }

  public SequenceException(String message, Throwable cause) {
    super(message, cause);
  }

  public SequenceException(Throwable cause) {
    super(cause);
  }
}
