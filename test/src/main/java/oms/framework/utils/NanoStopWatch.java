package oms.framework.utils;

import java.util.concurrent.TimeUnit;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import oms.framework.testing.Observer;
import oms.framework.testing.ObserverImpl;
import oms.sample.logger.PrettyLoggerConnector;

public class NanoStopWatch implements TestRule {
  private final Clock clock;
  private volatile long startNanos;
  private volatile long endNanos;
  private Observer observer;

  public NanoStopWatch() {
    this(new Clock());
    observer = new ObserverImpl(new PrettyLoggerConnector());
  }

  NanoStopWatch(Clock clock) {
    this.clock = clock;
    observer = new ObserverImpl(new PrettyLoggerConnector());
  }

  public NanoStopWatch(Observer observer) {
    this(new Clock());
    this.observer = observer;
  }

  /**
   * Gets the runtime for the test.
   *
   * @param unit
   *          time unit for returned runtime
   * @return runtime measured during the test
   */
  public long runtime(TimeUnit unit) {
    return unit.convert(getNanos(), TimeUnit.NANOSECONDS);
  }

  /**
   * Invoked when a test succeeds
   */
  private void logInfo(Description description, String status, long nanos) {
    String testName = description.getMethodName();

    String response = "";

    if (status.equals("finished")) {
      nanos = nanos - cleanUpTime;
      response += String.format("%s,time spent on cleaning up %s's resources was %d microseconds", status, testName,
          TimeUnit.NANOSECONDS.toMicros(nanos)) + " or " + TimeUnit.NANOSECONDS.toMicros(nanos) / 1000000.0
          + " in seconds";
    } else if (status.equals("started")) {
      response += String.format("Test %s has %s!", testName, status);
    } else {
      response += String.format("Test %s %s, spent %d microseconds", testName, status,
          TimeUnit.NANOSECONDS.toMicros(nanos)) + " or " + TimeUnit.NANOSECONDS.toMicros(nanos) / 1000000.0
          + " in seconds";

    }

    observer.observe(response);

  }

  Long cleanUpTime = 0L;

  protected void succeeded(long nanos, Description description) {
    logInfo(description, "succeeded", nanos);
    cleanUpTime = nanos;
  }

  protected void skipped(long nanos, Description description) {
    String testName = description.getMethodName();
    observer.startObservation();
    observer.observe(String.format("Test %s was skipped, no Time logged", testName));
    observer.endObservation();
  }

  protected void failed(long nanos, Throwable e, Description description) {
    logInfo(description, "failed", nanos);
  }

  protected void finished(long nanos, Description description) {
    logInfo(description, "finished", nanos);
    observer.endObservation();
  }

  private long getNanos() {
    if (startNanos == 0) {
      throw new IllegalStateException("Test has not started");
    }
    long currentEndNanos = endNanos; // volatile read happens here
    if (currentEndNanos == 0) {
      currentEndNanos = clock.nanoTime();
    }

    return currentEndNanos - startNanos;
  }

  private void starting(Description description) {
    observer.startObservation();
    startNanos = clock.nanoTime();
    logInfo(description, "started", 0);
    endNanos = 0;
  }

  private void stopping() {
    endNanos = clock.nanoTime();
  }

  public final Statement apply(Statement base, Description description) {
    return new InternalWatcher().apply(base, description);
  }

  private class InternalWatcher extends TestWatcher {

    @Override
    protected void starting(Description description) {
      NanoStopWatch.this.starting(description);
    }

    @Override
    protected void finished(Description description) {
      NanoStopWatch.this.finished(getNanos(), description);
      NanoStopWatch.this.stopping();
    }

    @Override
    protected void succeeded(Description description) {

      NanoStopWatch.this.succeeded(getNanos(), description);
    }

    @Override
    protected void failed(Throwable e, Description description) {
      NanoStopWatch.this.stopping();
      NanoStopWatch.this.failed(getNanos(), e, description);
    }

    @Override
    protected void skipped(AssumptionViolatedException e, Description description) {
      NanoStopWatch.this.stopping();
      NanoStopWatch.this.skipped(getNanos(), description);
    }
  }

  static class Clock {

    public long nanoTime() {
      return System.nanoTime();
    }
  }
}