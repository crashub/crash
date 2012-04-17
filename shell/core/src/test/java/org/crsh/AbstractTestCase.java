package org.crsh;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractTestCase extends TestCase {

  protected AbstractTestCase() {
  }

  protected AbstractTestCase(String name) {
    super(name);
  }

  public static AssertionFailedError failure(Throwable t) {
    AssertionFailedError afe = new AssertionFailedError();
    afe.initCause(t);
    return afe;
  }

  public static AssertionFailedError failure(Object message) {
    return new AssertionFailedError("" + message);
  }

  public static void safeFail(Throwable throwable) {
    if (throwable != null) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(throwable);
      throw afe;
    }
  }

  public static <T> T assertInstance(Class<T> expectedType, Object o) {
    if (expectedType.isInstance(o)) {
      return expectedType.cast(o);
    } else {
      throw failure("Was expecting the object " + o + " to be an instance of " + expectedType.getName());
    }
  }

  public static <T> T assertType(Class<T> expectedType, Object o) {
    if (o == null) {
      throw failure("Was expecting the object " + o + " to not be null");
    } else if (o.getClass().equals(expectedType)) {
      return expectedType.cast(o);
    } else {
      throw failure("Was expecting the object " + o + " to be an instance of " + expectedType.getName());
    }
  }

  public static void assertJoin(Thread thread) {
    assertJoin(thread, 5000);
  }

  public static void assertJoin(Thread thread, long timeMillis) {
    long before = System.currentTimeMillis();
    try {
      thread.join(timeMillis);
    }
    catch (InterruptedException e) {
      throw failure(e);
    }
    long after = System.currentTimeMillis();
    if (after - before >= timeMillis) {
      throw failure("Join failed");
    }
  }

}
