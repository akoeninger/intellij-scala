package org.jetbrains.sbt.shell

import com.intellij.codeEditor.printing.PrintAction
import com.intellij.execution.ExecutionManager
import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.execution.filters.PatternHyperlinkPart.{LINE, PATH}
import com.intellij.execution.filters.UrlFilter.UrlFilterProvider
import com.intellij.execution.filters._
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.ui.actions.CloseAction
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.{NextOccurenceToolbarAction, PreviousOccurenceToolbarAction}
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.sbt.shell.action.{ExecuteTaskAction, RestartAction, StopAction}

/**
  * Created by jast on 2017-05-17.
  */
class SbtShellConsoleView private(project: Project) extends
  LanguageConsoleImpl(project, SbtShellFileType.getName, SbtShellLanguage) {

  override def createConsoleActions(): Array[AnAction] = {
    val defaultActions =
      // yay inheritance over composition -_-
      super.createConsoleActions().filter {
        case _: PreviousOccurenceToolbarAction => false
        case _: NextOccurenceToolbarAction => false
        case _: PrintAction => false
        case _ => true
      }

    val myToolbarActions = Array(
      new RestartAction(project),
      new StopAction(project),
      new ExecuteTaskAction("products", Option(AllIcons.Actions.Compile))
    )

    defaultActions ++ myToolbarActions
  }

}

object SbtShellConsoleView {

  def apply(project: Project): SbtShellConsoleView = {
    val cv = new SbtShellConsoleView(project)
    cv.getConsoleEditor.setOneLineMode(true)

    // stack trace file links
    cv.addMessageFilter(new ExceptionFilter(GlobalSearchScope.allScope(project)))
    // other file links
    cv.addMessageFilter(filePatternFilters(project))
    // url links
    new UrlFilterProvider().getDefaultFilters(project).foreach(cv.addMessageFilter)

    cv
  }

  private def filePatternFilters(project: Project) = {
    import PatternHyperlinkPart._

    def pattern(patternMacro: String) = new RegexpFilter(project, patternMacro).getPattern

    // file with line number
    val fileWithLinePattern = pattern(s"${RegexpFilter.FILE_PATH_MACROS}:${RegexpFilter.LINE_MACROS}")
    // FILE_PATH_MACROS includes a capturing group at the beginning that the format only can handle if the first linkPart is null
    val fileWithLineFormat = new PatternHyperlinkFormat(fileWithLinePattern, false, false, null, PATH, LINE)

    // file output without lines in messages
    val fileOnlyPattern = pattern(s"${RegexpFilter.FILE_PATH_MACROS}")
    val fileOnlyFormat = new PatternHyperlinkFormat(fileOnlyPattern, false, false, null, PATH)

    val dataFinder = new PatternBasedFileHyperlinkRawDataFinder(Array(fileWithLineFormat, fileOnlyFormat))
    new PatternBasedFileHyperlinkFilter(project, null, dataFinder)
  }


}
