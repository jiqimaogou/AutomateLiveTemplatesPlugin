package scala.edu.jetbrains.plugin.lt.finder.common

import com.intellij.openapi.fileTypes.FileType
import scala.edu.jetbrains.plugin.lt.finder.miner.TreeEncoding

/**
  * Created by Dmitriy Baidin.
  */
class TemplateWithFileType(val template: Template,
                           val fileType: FileType)

case class Template(text: String,
                    treeEncoding: TreeEncoding,
                    templateStatistic: TemplateStatistic)

class TemplateStatistic(val placeholderCount: Int,
                        val nodeCount: Int,
                        val occurrenceCount: Int) {
  def placeholderToNodeRatio: Double = placeholderCount /
    nodeCount.toDouble
}