package org.jetbrains.plugins.scala.codeInspection.collections

import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.EditorTestUtil.{SELECTION_END_TAG, SELECTION_START_TAG}

/**
  * @author Adam Koeninger
  * 7/13/2017
  */
class OptionForeachInspectionTest extends OperationsOnCollectionInspectionTest {

  import EditorTestUtil.{SELECTION_END_TAG => END, SELECTION_START_TAG => START}

  override protected val classOfInspection: Class[_ <: OperationOnCollectionInspection] =
    classOf[OptionForeachInspection]

  override protected val hint: String = "Use option.foreach instead of emulating it"

  private val optionalValue = "val someValue: Option[Int] = Option(5);"
  
  def test_ifstmt_replace(): Unit = {
    val selected = s"$optionalValue ${START}if (someValue.isDefined) println(someValue.get)$END"

    checkTextHasError(selected)

    val text = s"$optionalValue if (someValue.isDefined) println(someValue.get)"
    val result = s"$optionalValue someValue.foreach(println)"

    doTest(selected, text, result)
  }
}
