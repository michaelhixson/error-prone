/*
 * Copyright 2017 The Error Prone Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.errorprone.bugpatterns.testdata;

import com.google.errorprone.annotations.Immutable;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Negative test cases for {@link ConstructorInvokesOverridable}. */
@Immutable
public class ConstructorInvokesOverridableNegativeCases {

  final int i = safeFinal();
  final int j = safeStatic();
  final int k = safePrivate();

  {
    safeFinal();
    safeStatic();
    safePrivate();
  }

  public ConstructorInvokesOverridableNegativeCases() {
    safeFinal();
    safeStatic();
    safePrivate();

    @SuppressWarnings("ConstructorInvokesOverridable")
    class Suppressed {
      final int suppressed = unsafe();
    }

    class SuppressedMembers {
      @SuppressWarnings("ConstructorInvokesOverridable")
      final int suppressed = unsafe();

      @SuppressWarnings("ConstructorInvokesOverridable")
      int suppressed() {
        return unsafe();
      }
    }

    // Safe: on a different instance.
    new ConstructorInvokesOverridableNegativeCases().localVariable();

    new ConstructorInvokesOverridableNegativeCases() {
      // Safe: calls its own method and cannot be subclassed because it's anonymous.
      final int i = unsafe();
      final int j = this.unsafe();
    };

    final class Local extends ConstructorInvokesOverridableNegativeCases {
      // Safe: calls its own method and cannot be subclassed because it's final.
      final int i = unsafe();
      final int j = this.unsafe();
      final int k = Local.this.unsafe();
    }

    class Parent extends ConstructorInvokesOverridableNegativeCases {
      class Inner extends ConstructorInvokesOverridableNegativeCases {
        // OK to call an overridable method of the containing class
        final int i = Parent.this.unsafe();
      }
    }

    new java.util.HashMap<String, String>() {
      {
        put("Hi", "Mom");
      }
    };

    new Thread() {
      @Override
      public void run() {
        safeFinal();
        safeStatic();
        safePrivate();
      }
    }.start();

    new Thread(() -> safeFinal()).start();
    new Thread(() -> safeStatic()).start();
    new Thread(() -> safePrivate()).start();
  }

  public void localVariable() {
    // Safe because this variable is not a field
    int i = unsafe();
  }

  @RunWith(JUnit4.class)
  public static class JUnitTest {
    // Safe because we skip the check in unit tests.
    final int i = unsafe();

    protected int unsafe() {
      return 3;
    }
  }

  /** Not overridable because final */
  protected final int safeFinal() {
    return 0;
  }

  /** Not overridable because static */
  protected static int safeStatic() {
    return 1;
  }

  /** Not overridable because private */
  private int safePrivate() {
    return 2;
  }

  protected int unsafe() {
    return 3;
  }

  void localInitializer() {
    class Local {
      {
        // safe because unsafe is not a member of this class
        unsafe();
      }
    }
    class Local2 extends ConstructorInvokesOverridableNegativeCases {
      {
        // same as above, but try to confuse the bug pattern
        ConstructorInvokesOverridableNegativeCases.this.unsafe();
      }
    }
  }

  // Lookup is handled correctly for inner classes as well
  class Inner {
    // OK to call an overridable method of the containing class
    final int safeValue = unsafe();
  }
  class Inner2 extends ConstructorInvokesOverridableNegativeCases {
    // same as above, but try to confuse the bug pattern
    final int safeValue = ConstructorInvokesOverridableNegativeCases.this.unsafe();
  }
}
