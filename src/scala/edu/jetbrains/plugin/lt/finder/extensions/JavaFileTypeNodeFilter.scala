package scala.edu.jetbrains.plugin.lt.finder.extensions

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.fileTypes.FileType
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.plugins.scala.ScalaFileType

import scala.edu.jetbrains.plugin.lt.finder.miner.{InnerNodeId, NodeId}


trait FileTypeNodeFilter {
  def shouldAnalyze(nodeId: NodeId): Boolean

  def fileType: FileType
}

object FileTypeNodeFilter {
  val EP_NAME: ExtensionPointName[FileTypeNodeFilter] = ExtensionPointName.create("edu.jetbrains.plugin.lt.fileTypeNodeFilter")
}

class JavaFileTypeNodeFilter extends FileTypeNodeFilter {

  import com.intellij.psi.impl.source.tree.JavaDocElementType._
  import com.intellij.psi.impl.source.tree.JavaElementType._

  def shouldAnalyze(nodeId: NodeId): Boolean = nodeId match {
    case InnerNodeId(elementType) =>
      elementType != IMPORT_STATEMENT && elementType != IMPORT_STATIC_STATEMENT && elementType != PACKAGE_STATEMENT && elementType != DOC_COMMENT
    case _ => true
  }

  override def fileType: FileType = JavaFileType.INSTANCE
}

class ScalaFileTypeNodeFilter extends FileTypeNodeFilter {

  import org.jetbrains.plugins.scala.lang.parser.ScalaElementTypes._

  override def shouldAnalyze(nodeId: NodeId): Boolean = nodeId match {
    case InnerNodeId(elementType) =>
      elementType != IMPORT_STMT && elementType != PACKAGING
    case _ => true
  }

  override def fileType: FileType = ScalaFileType.INSTANCE
}

class KotlinFileTypeNodeFilter extends FileTypeNodeFilter {

  import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes._

  override def shouldAnalyze(nodeId: NodeId): Boolean = nodeId match {
    case InnerNodeId(elementType) =>
      elementType != IMPORT_DIRECTIVE  && elementType != PACKAGE_DIRECTIVE
    case _ => true
  }

  override def fileType: FileType = KotlinFileType.INSTANCE
}


object DefaultFileTypeNodeFilter extends FileTypeNodeFilter {
  override def shouldAnalyze(nodeId: NodeId): Boolean = true

  override def fileType: FileType = ???
}