package scala.edu.jetbrains.plugin.lt.finder.postprocessor

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.fileTypes.FileType
import scala.edu.jetbrains.plugin.lt.finder.miner.{EncodeNode, InnerNodeId, LeafNodeId, PathNode, Placeholder, TreeEncoding}

trait TreeEncodingFormatter {
  def process(treeEncoding: TreeEncoding): (String, TreeEncoding)

  def fileType: FileType
}

object TreeEncodingFormatter {
  val EP_NAME: ExtensionPointName[TreeEncodingFormatter] =
    ExtensionPointName.create("edu.jetbrains.plugin.lt.treeEncodingFormatter")
}

class DefaultTreeEncodingFormatter extends TreeEncodingFormatter {

  override def process(treeEncoding: TreeEncoding): (String, TreeEncoding) = {
    val encodeList = compressEncodeList(treeEncoding.encodeList)
    (toString(encodeList), TreeEncoding(encodeList))
  }

  protected val compressiblePattern: String = "(\\s|\\n|\\t)*"

  def compressEncodeList(encodeList: List[PathNode]): List[PathNode] = {
    def helper(encodeList: List[PathNode], prevIsPlaceholder: Boolean, accList: List[PathNode]): List[PathNode] = encodeList match {
      case node :: tail =>
        node match {
          case EncodeNode(leaf: LeafNodeId) =>
            if (leaf.nodeText.matches(compressiblePattern)) {
              if (prevIsPlaceholder)
                helper(tail, prevIsPlaceholder, accList)
              else
                helper(tail, prevIsPlaceholder, node :: accList)
            }
            else
              helper(tail, prevIsPlaceholder = false, node :: accList)

          case EncodeNode(_: InnerNodeId) =>
            helper(tail, prevIsPlaceholder, accList)
          case Placeholder =>
            if (prevIsPlaceholder)
              helper(tail, prevIsPlaceholder = true, accList)
            else
              helper(tail, prevIsPlaceholder = true, node :: accList)
        }
      case Nil => accList
    }

    helper(encodeList, prevIsPlaceholder = false, List.empty)
  }

  def toString(encodeList: List[PathNode]): String = {
    encodeList.map {
      case EncodeNode(leaf: LeafNodeId) =>
        leaf.nodeText
      case Placeholder =>
        " #_# "
      case _ => ""
    }.mkString
  }

  override def fileType: FileType = ???
}

class JavaTreeEncodingFormatter extends DefaultTreeEncodingFormatter {
  override def fileType: FileType = JavaFileType.INSTANCE

  override protected val compressiblePattern: String = "(\\s|\\n|\\t|\\.|;)*"

  override def compressEncodeList(encodeList: List[PathNode]): List[PathNode] = {
    import com.intellij.psi.JavaTokenType._
    var result = super.compressEncodeList(encodeList)

    def helper(list: List[PathNode]): List[PathNode] = list match {
      case Placeholder :: EncodeNode(LeafNodeId(LPARENTH, "(")) :: EncodeNode(LeafNodeId(RPARENTH, ")")) :: Placeholder :: xs => Placeholder :: helper(xs)
      case x :: xs => x :: helper(xs)
      case _ => Nil
    }

    if (result.headOption.collect {
      case EncodeNode(leaf: LeafNodeId) if leaf.nodeText == "{" => true
    }.isDefined &&
      result.lastOption.collect {
        case EncodeNode(leaf: LeafNodeId) if leaf.nodeText == "}" => true
      }.isDefined) {
      result = result.take(result.size - 1).tail
    }
    helper(result)
  }
}