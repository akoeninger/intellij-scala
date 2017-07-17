package org.jetbrains.plugins.scala.codeInspection.collections

import org.jetbrains.plugins.scala.codeInspection.InspectionBundle
import com.intellij.codeInsight.PsiEquivalenceUtil
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScExpression, ScMethodCall}

/** @author Adam.Koeninger
 *  07/12/2017
 */
class OptionForeachInspection extends OperationOnCollectionInspection {
  override def possibleSimplificationTypes: Array[SimplificationType] = Array(OptionForeach)
}

object OptionForeach extends SimplificationType {
  override def hint: String = "Use option.foreach instead of emulating it"

  override def getSimplification(expr: ScExpression): Option[Simplification] = {
    expr match {
      case exp@IfThenStmt(qual`.isDefined`(), ScMethodCall(method, Seq(methodArg)))
        if replaceIfEquivalent(qual, methodArg) =>
        Some(replace(exp).withText(invocationText(qual, "foreach", method)))
      case _ => None
    }
  }

  private def replaceIfEquivalent(expression: ScExpression, methodArgument: ScExpression) = {
    methodArgument match {
      case qual`.get`() => PsiEquivalenceUtil.areElementsEquivalent(expression, qual)
      case _ => false
    }
  }
}
