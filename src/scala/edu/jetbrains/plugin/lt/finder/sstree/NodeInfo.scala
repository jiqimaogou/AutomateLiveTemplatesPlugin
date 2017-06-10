package scala.edu.jetbrains.plugin.lt.finder.sstree

import scala.edu.jetbrains.plugin.lt.finder.common.NodeId

import scala.collection.mutable

sealed trait NodeInfo {
  def stat: NodeStat
}

final class LeafNodeInfo(val stat: NodeStat) extends NodeInfo

final class InnerNodeInfo(val stat: NodeStat,
                          val childrenAlternatives: mutable.Map[List[NodeId], List[NodeInfo]]) extends NodeInfo

final class NodeStat {
  var occurrenceCount: Int = 0

  def ++ = {
    occurrenceCount += 1
    this
  }
}