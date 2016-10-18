/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.fixes.junit;

import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.IGQuickFixesTestCase;
import com.siyeh.ig.junit.JUnit4AnnotatedMethodInJUnit3TestCaseInspection;

/**
 * @author Bas Leijdekkers
 */
public class ConvertToJUnit4FixTest extends IGQuickFixesTestCase {

  public void testOtherMethods() { doTest(); }
  public void testLocalMethod() { doTest(); }

  @Override
  protected void doTest() {
    doTest(InspectionGadgetsBundle.message("convert.junit3.test.class.quickfix", getTestName(false)));
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new JUnit4AnnotatedMethodInJUnit3TestCaseInspection());
    myRelativePath = "junit/convert_to_junit4";

    myFixture.addClass("package org.junit;" +
                       "public class Assert {" +
                       "  public static void assertTrue(boolean condition) {}" +
                       "  public static void assertFalse(boolean condition) {}" +
                       "}");
    myFixture.addClass("package org.junit;" +
                       "@Retention(RetentionPolicy.RUNTIME)" +
                       "@Target({ElementType.METHOD})" +
                       "public @interface Test {}");
    myFixture.addClass("package junit.framework;" +
                       "public class Assert {" +
                       "  public static void assertTrue(boolean condition) {}" +
                       "  public static void assertFalse(String message, boolean condition) {}" +
                       "}");
    myFixture.addClass("package junit.framework;" +
                       "public abstract class TestCase extends Assert {}");
  }
}
