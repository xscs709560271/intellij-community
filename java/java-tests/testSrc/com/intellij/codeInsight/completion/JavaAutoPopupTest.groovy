/*
 * Copyright 2000-2010 JetBrains s.r.o.
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
package com.intellij.codeInsight.completion

import com.intellij.openapi.actionSystem.IdeActions

 /**
 * @author peter
 */
class JavaAutoPopupTest extends CompletionAutoPopupTestCase {

  public void testNewItemsOnLongerPrefix() {
    myFixture.configureByText("a.java", """
      class Foo {
        void foo(String iterable) {
          <caret>
        }
      }
    """)
    type('i')
    assertSameElements myFixture.lookupElementStrings, "if", "iterable", "int"

    type('t')
    assertOrderedEquals myFixture.lookupElementStrings, "iterable"
    assertEquals 'iterable', lookup.currentItem.lookupString

    type('er')
    assert !lookup
    //assertOrderedEquals myFixture.lookupElementStrings, "iter", "iterable"
    //assertEquals 'iter', lookup.currentItem.lookupString
  }

  public void _testRecalculateItemsOnBackspace() {
    myFixture.configureByText("a.java", """
      class Foo {
        void foo(String iterable) {
          int itaa;
          ite<caret>
        }
      }
    """)
    type "r"
    assertOrderedEquals myFixture.lookupElementStrings, "iter", "iterable"

    println "b1"
    type '\b'
    assertOrderedEquals myFixture.lookupElementStrings, "iterable"

    println "b2"
    type '\b'
    println "typed backspace"
    assertOrderedEquals myFixture.lookupElementStrings, "itaa", "iterable"
    type "a"
    assertOrderedEquals myFixture.lookupElementStrings, "itaa"
    type '\b'
    assertOrderedEquals myFixture.lookupElementStrings, "itaa", "iterable"
    type "e"
    assertOrderedEquals myFixture.lookupElementStrings, "iterable"

    println "typing r"
    type "r"
    assertOrderedEquals myFixture.lookupElementStrings, "iter", "iterable"
  }

  public void testExplicitSelectionShouldSurvive() {
    myFixture.configureByText("a.java", """
      class Foo {
        void foo(String iterable) {
          int iterable2;
          it<caret>
        }
      }
    """)
    type "e"
    assertOrderedEquals myFixture.lookupElementStrings, "iterable", "iterable2"

    assertEquals 'iterable', lookup.currentItem.lookupString
    myFixture.performEditorAction IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN
    assertEquals 'iterable2', lookup.currentItem.lookupString

    type "r"
    assertOrderedEquals myFixture.lookupElementStrings, "iter", "iterable", 'iterable2'
    assertEquals 'iterable2', lookup.currentItem.lookupString

  }

  public void testExplicitMouseSelectionShouldSurvive() {
    myFixture.configureByText("a.java", """
      class Foo {
        void foo(String iterable) {
          int iterable2;
          it<caret>
        }
      }
    """)
    type "e"
    assertOrderedEquals myFixture.lookupElementStrings, "iterable", "iterable2"

    assertEquals 'iterable', lookup.currentItem.lookupString
    lookup.currentItem = lookup.items[1]
    assertEquals 'iterable2', lookup.currentItem.lookupString

    type "r"
    assertOrderedEquals myFixture.lookupElementStrings, "iter", "iterable", 'iterable2'
    assertEquals 'iterable2', lookup.currentItem.lookupString

  }

  public void testNoAutopopupInTheMiddleOfIdentifier() {
    myFixture.configureByText("a.java", """
      class Foo {
        String foo(String iterable) {
          return it<caret>rable;
        }
      }
    """)
    type 'e'
    assertNull lookup
  }

  public void testGenerallyFocusLookupInJavaMethod() {
    myFixture.configureByText("a.java", """
      class Foo {
        String foo(String iterable) {
          return it<caret>;
        }
      }
    """)
    type 'e'
    assertTrue lookup.focused
  }

  public void testNoLookupFocusInJavaVariable() {
    myFixture.configureByText("a.java", """
      class Foo {
        String foo(String st<caret>) {
        }
      }
    """)
    type 'r'
    assertFalse lookup.focused
  }

  public void testNoStupidNameSuggestions() {
    myFixture.configureByText("a.java", """
      class Foo {
        String foo(String <caret>) {
        }
      }
    """)
    type 'r'
    assertNull lookup
  }

  public void testExplicitSelectionShouldBeHonoredFocused() {
    myFixture.configureByText("a.java", """
      class Foo {
        String foo() {
          int abcd;
          int abce;
          a<caret>
        }
      }
    """)
    type 'b'
    assert lookup.focused
    type 'c'

    assertOrderedEquals myFixture.lookupElementStrings, 'abcd', 'abce'
    assertEquals 'abcd', lookup.currentItem.lookupString
    lookup.currentItem = lookup.items[1]
    assertEquals 'abce', lookup.currentItem.lookupString

    type '\t'
    myFixture.checkResult """
      class Foo {
        String foo() {
          int abcd;
          int abce;
          abce<caret>
        }
      }
    """
  }

  public void testHideAutopopupIfItContainsExactMatch() {
    myFixture.configureByText("a.java", """
      class Foo {
        String foo() {
          int abcd;
          int abcde;
          int abcdefg;
          ab<caret>
        }
      }
    """)
    type 'c'
    assert lookup
    type 'd'
    assert !lookup
    type 'e'
    assert !lookup
    type 'f'
    assert lookup
  }

}
