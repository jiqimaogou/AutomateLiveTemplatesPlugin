package scala.edu.jetbrains.plugin.lt.finder.common

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType

/**
  * Base class for node's id
  * Id should describe node, using in test for equality
  */
sealed abstract class NodeId() {
  def elementType: IElementType
}

object NodeId {
  def apply(astNode: ASTNode, childrenCount: Int): NodeId = childrenCount match {
    case 0 =>
      LeafNodeId(
        astNode.getElementType,
        astNode.getText.replaceAll(" +", " "))
    case n =>
      InnerNodeId(
        astNode.getElementType,
        n
      )
  }
}

/**
  * Identifier of node which has children
  *
  * @param elementType   node type
  * @param childrenCount count of children
  */
case class InnerNodeId(elementType: IElementType,
                       childrenCount: Int) extends NodeId

/**
  * Identifier of leaf node
  *
  * @param elementType node type
  * @param nodeText    text of node
  */
case class LeafNodeId(elementType: IElementType,
                      nodeText: String) extends NodeId

