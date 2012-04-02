package org.crsh;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractTestCase extends TestCase {


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
}
