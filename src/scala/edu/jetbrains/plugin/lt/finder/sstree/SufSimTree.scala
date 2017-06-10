package scala.edu.jetbrains.plugin.lt.finder.sstree

import com.intellij.lang.ASTNode
import scala.edu.jetbrains.plugin.lt.finder.common.{InnerNodeId, LeafNodeId, NodeId}

import scala.collection.mutable


/**
  * SufTree for astTree
  */
final class SufSimTree {

  val treeRoot: mutable.Map[NodeId, NodeInfo] = mutable.Map.empty
  private val nodeIdMap: mutable.Map[NodeId, NodeId] = mutable.Map.empty

  def addTree(astNode: ASTNode): Unit = addAllPrefix(transform(astNode))

  /**
    * Transform tree of ASTNodes -> to tree of Node
    */
  private def transform(astNode: ASTNode): Node = {
    def getChildren(astNode: ASTNode): (Int, List[ASTNode]) = {
      var child = astNode.getFirstChildNode

      var childrenCount = 0
      var result: List[ASTNode] = List.empty

      while (child != null) {
        childrenCount += 1
        result ::= child
        child = child.getTreeNext
      }

      (childrenCount, result.reverse)
    }

    def getNodeId(astNode: ASTNode, childrenCount: Int): NodeId = {
      val nodeId = NodeId(astNode, childrenCount)
      nodeIdMap.getOrElseUpdate(nodeId, nodeId)
    }

    val (childrenCount, children) = getChildren(astNode)
    val nodeId = getNodeId(astNode, childrenCount)

    nodeId match {
      case leafId: LeafNodeId => new Leaf(leafId)
      case innerNodeId: InnerNodeId =>
        new InnerNode(innerNodeId, children.map(transform))
    }
  }

  /**
    * Add all subtrees to sufTree
    */
  private def addAllPrefix(node: Node): Unit = {
    addTree(node)
    node match {
      case innerNode: InnerNode =>
        innerNode.children.foreach(addAllPrefix)
      case leaf: Leaf =>
    }
  }

  /**
    * Add tree to sufTree
    */
  private def addTree(root: Node): Unit = {
    def createNodeInfo(node: Node): NodeInfo = node match {
      case _: Leaf => LeafNodeInfo.empty
      case _: InnerNode => InnerNodeInfo.empty
    }

    def dfs(node: Node, nodeInfo: NodeInfo): Unit = (node, nodeInfo) match {
      case (leafNode: Leaf, leafInfo: LeafNodeInfo) => leafInfo.stat ++
      case (innerNode: InnerNode, innerInfo: InnerNodeInfo) =>
        innerInfo.stat.++
        val nodeInfoList = innerInfo
          .childrenAlternatives
          .getOrElseUpdate(
            innerNode
              .children
              .map(_.nodeId),
            innerNode
              .children
              .map(createNodeInfo)
          )
        innerNode.children.zip(nodeInfoList)
          .foreach { case (childNode, childInfo) =>
            dfs(childNode, childInfo)
          }
      case _ => throw new RuntimeException("Type incompatible")
    }

    val nodeInfo = treeRoot.getOrElseUpdate(root.nodeId, createNodeInfo(root))
    dfs(root, nodeInfo)
  }

  sealed trait Node {
    def nodeId: NodeId
  }

  final class InnerNode(val nodeId: NodeId,
                        val children: List[Node]) extends Node

  final class Leaf(val nodeId: NodeId) extends Node

  object LeafNodeInfo {
    def empty: LeafNodeInfo = new LeafNodeInfo(new NodeStat)
  }

  object InnerNodeInfo {
    def empty: InnerNodeInfo = new InnerNodeInfo(new NodeStat, mutable.Map.empty)
  }

}
