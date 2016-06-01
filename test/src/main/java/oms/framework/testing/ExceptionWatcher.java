package oms.framework.testing;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import oms.framework.exception.ExceptionUtils;

public class ExceptionWatcher extends TestWatcher {

  @Override
  protected void failed(Throwable e, Description description) {

    String message = ExceptionUtils.getReadableMessage(e);
    System.out.println(message);
  }

}