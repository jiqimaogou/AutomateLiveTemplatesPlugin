package scala.edu.jetbrains.plugin.lt.finder.miner

import com.intellij.lang.ASTNode
import scala.edu.jetbrains.plugin.lt.finder.common.Template
import scala.edu.jetbrains.plugin.lt.finder.extensions.FileTypeNodeFilter
import scala.edu.jetbrains.plugin.lt.finder.postprocessor.DefaultTreeEncodingFormatter
import scala.edu.jetbrains.plugin.lt.finder.sstree.TemplateSearchConfiguration

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}


/**
  * Class implements MB3 algorithm.
  * It returns frequent induced trees in forest.
  *
  * @param minerConfiguration          parameters of MB3 algorithm
  * @param nodeFilter              filter of nodes
  */
class MB3(val minerConfiguration: MinerConfiguration,
          val nodeFilter: FileTypeNodeFilter) {

  /**
    * Get templates of forest.
    *
    * @param roots root nodes of files
    * @return templates
    */
  def getFrequentTreeEncodings(roots: Seq[ASTNode]): List[TreeEncodingWithCount] = {
    val (dict, minSupport) = buildDictionary(roots)
    println(s"Dictionary length ${dict.length}")
    getEncodingCandidates(minSupport, dict)
  }


  /**
    * Builds dictionary from nodes.
    * Dictionary is array of all nodes in pre order traversal.
    *
    * @param roots root nodes of files
    * @return dictionary and min support count
    */
   def buildDictionary(roots: Seq[ASTNode]): (ArrayBuffer[DictionaryNode], Int) = {
    val nodeIdToCount: mutable.Map[NodeId, Int] = mutable.Map.empty

    /**
      * Add occurrence count for node id
      *
      * @param nodeId node id
      */
    def addOccurrence(nodeId: NodeId): Unit =
      nodeIdToCount += (nodeId -> (nodeIdToCount.getOrElse(nodeId, 0) + 1))

    /**
      * Optimization to maintain only one instance of each node id
      */
    val nodeIdMap: mutable.Map[NodeId, NodeId] = mutable.Map.empty

    val result: ArrayBuffer[DictionaryNode] = new ArrayBuffer[DictionaryNode]()

    /**
      * Get node id by ast node and children count.
      *
      * @param astNode       ast node
      * @param childrenCount children count
      * @return node id
      */
    def getNodeId(astNode: ASTNode, childrenCount: Int): NodeId = {
      val nodeId = NodeId(astNode, childrenCount)
      nodeIdMap.getOrElseUpdate(nodeId, nodeId)
    }

    /**
      * DFS traversal by ast tree.
      *
      * @param curNode  current ast node
      * @param curDepth current depth in ast tree
      */
    def dfs(curNode: ASTNode, curDepth: Int): Unit = {
      def childCountNext(child: ASTNode, childrenCountAcc: Int): Int = child match {
        case null => childrenCountAcc
        case childNode => childCountNext(childNode.getTreeNext, childrenCountAcc + 1)
      }

      def dfsNext(child: ASTNode): Unit = child match {
        case null =>
        case childNode =>
          dfs(
            curNode = childNode,
            curDepth = curDepth + 1)

          dfsNext(childNode.getTreeNext)
      }

      val childCount = childCountNext(curNode.getFirstChildNode, 0)

      val nodeId = getNodeId(curNode, childCount)

      addOccurrence(nodeId)

      val shouldAnalyze = nodeFilter.shouldAnalyze(nodeId)

      val dictNode: DictionaryNode =
        if (shouldAnalyze) {
          new Node(
            nodeId = nodeId,
            depth = curDepth)
        } else {
          new DictionaryPlaceholder(
            depth = curDepth)
        }

      result += dictNode
      if (shouldAnalyze) {
        dfsNext(curNode.getFirstChildNode)
      }

      dictNode.rightmostLeafPos = result.size - 1
    }

    roots.foreach { root =>
      dfs(root, 0)
    }

    /**
      * Get frequent nodes.
      *
      * @param nodeIdToCount map node id to occurrence count
      * @return set of frequent nodes and min support
      */
    def getFreqNodes(nodeIdToCount: mutable.Map[NodeId, Int]): (Set[NodeId], Int) = {
      val nodeCount = nodeIdToCount.size

      val nodeOccurrenceCount = nodeIdToCount.values.sum
      val minSupport = ((nodeOccurrenceCount / nodeCount) * minerConfiguration.minSupportCoefficient).toInt
      println(s"Node count: $nodeCount")
      println(s"Node occurrence count: $nodeOccurrenceCount")
      println(s"Min support: $minSupport")

      (nodeIdToCount.filter(_._2 >= minSupport).keys.toSet, minSupport.max(1))
    }

    val (freqNodes, minSupport) = getFreqNodes(nodeIdToCount)

    result.transform {
      case node: Node =>
        if (freqNodes(node.nodeId)) node else new DictionaryPlaceholder(depth = node.depth)
      case d => d
    }

    (result, minSupport)
  }

  /**
    * Get tree encoding candidates.
    *
    * @param minSupport min count of occurrence encoding in tree
    * @param dictionary array of all nodes in pre order
    * @return tree encoding candidates
    */
  private def getEncodingCandidates(minSupport: Int,
                                    dictionary: ArrayBuffer[DictionaryNode]): List[TreeEncodingWithCount] = {
    val occurrenceMap = mutable.Map.empty[(TreeEncoding, List[PathNode]), mutable.Buffer[Occurrence]]

    for (
      i <- dictionary.indices
    ) {
      dictionary(i) match {
        case node: Node => node.nodeId match {
          case inner: InnerNodeId =>
            val encode = TreeEncoding(List(EncodeNode(inner)))
            new Occurrence(i, i) +=: occurrenceMap.getOrElseUpdate((encode, List.empty), new ListBuffer[Occurrence]())
          case _ =>
        }
        case _ =>
      }
    }

    def extendMap(occMap: mutable.Map[(TreeEncoding, List[PathNode]), mutable.Buffer[Occurrence]]): List[TreeEncodingWithCount] = {
      occMap.par.flatMap { case ((enc, parentPath), encList) =>
        val (newCandidates, occurrenceCount, isTemplate) = extend(enc, encList, parentPath, minSupport, dictionary)
        val result = extendMap(newCandidates)
        if (isTemplate) TreeEncodingWithCount(enc, occurrenceCount) :: result else result
      }.toList
    }

    extendMap(occurrenceMap)
  }

  /**
    * Try to add new node to candidates.
    * And check current encoding has enough occurrence count in dictionary.
    *
    * @param prefixEncoding current prefix encoding.
    * @param occurrenceList list of all occurrence in dictionary of prefix
    * @param minSupport     min count of occurrence encoding in tree
    * @param dictionary     array of all nodes in pre order
    * @return map of encoding to occurrence list and flag that indicates whether to add to candidates the prefix encoding
    */
  private def extend(prefixEncoding: TreeEncoding,
                     occurrenceList: mutable.Buffer[Occurrence],
                     parentPath: List[PathNode],
                     minSupport: Int,
                     dictionary: ArrayBuffer[DictionaryNode]): (mutable.Map[(TreeEncoding, List[PathNode]), mutable.Buffer[Occurrence]], Int, Boolean) = {
    val completedBuckets: mutable.HashMap[(TreeEncoding, List[PathNode]), mutable.Buffer[Occurrence]] = mutable.HashMap.empty
    val uncompletedBuckets: mutable.LinkedHashMap[(TreeEncoding, List[PathNode]), mutable.HashSet[Occurrence]] = mutable.LinkedHashMap.empty

    val unplacedRoots: mutable.HashSet[Int] = mutable.HashSet.empty
    occurrenceList.foreach(o => unplacedRoots += o.rootPos)
    val totalCount = unplacedRoots.size

    val placedRoots: mutable.HashSet[Int] = mutable.HashSet.empty

    val rightPosMap = mutable.Map.empty[Int, Int]
    occurrenceList.foreach(o => rightPosMap += (o.rootPos -> o.rightLeafPos))

    val maxDepthMap: mutable.Map[Int, Int] = mutable.HashMap.empty
    occurrenceList.foreach(o => maxDepthMap += (o.rootPos -> (dictionary(rightPosMap(o.rootPos)).depth + 1)))

    val parentPathMap: mutable.Map[Int, List[PathNode]] = mutable.HashMap.empty
    occurrenceList.foreach(o => parentPathMap += (o.rootPos -> parentPath))

    var first = true
    var changed = false

    def addOccurrence(treeEncoding: TreeEncoding, curParentPath: List[PathNode], occurrence: Occurrence): Unit = {
      if (completedBuckets.contains((treeEncoding, curParentPath))) {
        unplacedRoots -= occurrence.rootPos
        placedRoots += occurrence.rootPos
        occurrence +=: completedBuckets((treeEncoding, curParentPath))
      } else {
        rightPosMap += (occurrence.rootPos -> occurrence.rightLeafPos)
        val bucket = uncompletedBuckets.getOrElseUpdate((treeEncoding, curParentPath), {
          new mutable.HashSet[Occurrence]()
        }) += occurrence
        if (bucket.size >= minSupport) changed = true
      }
    }

    def next(rootPos: Int): Unit = {
      val rightmostLeaf = dictionary(rootPos).rightmostLeafPos
      var found = false
      var pos = rightPosMap(rootPos)
      var curDepth = dictionary(pos).depth
      var maxDepth = maxDepthMap(rootPos)
      var parentPath = parentPathMap(rootPos)
      while (!found && pos < rightmostLeaf) {
        pos += 1
        val dictNode = dictionary(pos)
        val depth = dictNode.depth
        if (depth <= maxDepth) {
          maxDepth = depth
          maxDepthMap += (rootPos -> maxDepth)

          val depthDiff = curDepth - depth
          if (depthDiff > 0) {
            parentPath = parentPath.drop(depthDiff)
          } else if (depthDiff < 0) {
            parentPath = prefixEncoding.encodeList.headOption.fold(List.empty[PathNode]) { head => head :: parentPath }
          }
          parentPathMap += (rootPos -> parentPath)

          curDepth = depth

          dictNode match {
            case node: Node =>

              found = true

              val encodeNode = EncodeNode(node.nodeId)
              val newOccurrence = new Occurrence(rootPos, pos)

              val treeEncodings = (if (first && rightPosMap(rootPos) == pos - 1) {
                List(encodeNode :: Nil, encodeNode :: Placeholder :: Nil)
              } else {
                List(encodeNode :: Placeholder :: Nil)
              }).map(TreeEncoding(_))

              treeEncodings.foreach(addOccurrence(_, parentPath, newOccurrence))

            case _ =>
          }
        }
      }
      if (!found) {
        unplacedRoots -= rootPos
        val newOccurrence = new Occurrence(rootPos, pos)
        rightPosMap += (newOccurrence.rootPos -> pos)
      }
    }

//    if (TreeEncoding(prefixEncoding.encodeList.reverse).toString.contains("@Override\n\tprotected  #_#   #_# (")) {
//      println("catch it")
//    }

    while (unplacedRoots.nonEmpty) {
      changed = false
      unplacedRoots.foreach(next)
      if (changed) {
        uncompletedBuckets.transform { case (enc, occList) =>
          if (occList.size >= minSupport) {
            val filteredOccList = occList.filterNot(o => placedRoots.contains(o.rootPos))
            if (filteredOccList.size >= minSupport) {
              filteredOccList.foreach { o =>
                placedRoots += o.rootPos
                unplacedRoots -= o.rootPos
              }
              completedBuckets += (enc -> filteredOccList.toBuffer)
              mutable.HashSet.empty
            } else {
              filteredOccList
            }
          } else {
            occList
          }
        }
      }
      first = false
    }

//    if (totalCount - placedRoots.size >= minSupport) {
//      println(TreeEncoding(prefixEncoding.encodeList.reverse).toString)
//      println("______________")
//    }

    (completedBuckets.map { case ((enc, parentPathNew), occList) =>
      (TreeEncoding(enc.encodeList ::: prefixEncoding.encodeList), parentPathNew) -> occList
    }, totalCount - placedRoots.size, totalCount - placedRoots.size >= minSupport)
  }


  def printBucketsByString(str: String, buckets: mutable.LinkedHashMap[(TreeEncoding, List[PathNode]), mutable.HashSet[Occurrence]], dictionary: ArrayBuffer[DictionaryNode]): Unit = {
    buckets.filter(_._1._1.toString == str).values.foreach(printOccurrenceList(_, dictionary))
  }

  def printOccurrenceList(occurrenceList: mutable.HashSet[Occurrence], dictionary: ArrayBuffer[DictionaryNode]): Unit = {
    occurrenceList.foreach { occ =>
      println(TreeEncoding((occ.rootPos to occ.rightLeafPos).map(dictionary).map {
        case node: Node => EncodeNode(node.nodeId)
        case _ => Placeholder
      }.toList).toString)
      println("_________________")
    }
  }
}

class Occurrence(val rootPos: Int,
                 val rightLeafPos: Int) {
  override def toString: String = s"{root: $rootPos, right: $rightLeafPos}"


  def canEqual(other: Any): Boolean = other.isInstanceOf[Occurrence]

  override def equals(other: Any): Boolean = other match {
    case that: Occurrence =>
      (that canEqual this) &&
        rootPos == that.rootPos
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(rootPos)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

class DictionaryNode(val depth: Int,
                     var rightmostLeafPos: Int)


class Node(val nodeId: NodeId,
           override val depth: Int) extends DictionaryNode(depth, -1) {
  override def toString = s"Node($nodeId, $depth)"
}

class DictionaryPlaceholder(override val depth: Int) extends DictionaryNode(depth, -1) {

  override def toString = s"DictionaryPlaceholder($depth)"
}

case class TreeEncoding(encodeList: List[PathNode]) {
  import TreeEncoding._
  override def toString: String = formatter.process(this)._1
}

object TreeEncoding {
  private val formatter: DefaultTreeEncodingFormatter = new DefaultTreeEncodingFormatter
}

trait PathNode

case class EncodeNode(nodeId: NodeId) extends PathNode

object Up extends PathNode

object Placeholder extends PathNode

case class TreeEncodingWithCount(treeEncoding: TreeEncoding, count: Int)