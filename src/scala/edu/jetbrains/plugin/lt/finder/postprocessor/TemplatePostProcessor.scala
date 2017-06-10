package scala.edu.jetbrains.plugin.lt.finder.postprocessor

import com.abahgat.suffixtree.GeneralizedSuffixTree
import scala.edu.jetbrains.plugin.lt.finder.common.{Template, TemplateStatistic}
import scala.edu.jetbrains.plugin.lt.finder.miner.{Placeholder, TreeEncoding, TreeEncodingWithCount}
import scala.edu.jetbrains.plugin.lt.finder.sstree.TemplateFilter


trait TemplatePostProcessor {

  def process(treeEncodings: Seq[TreeEncodingWithCount]): Seq[Template]

}

abstract class DefaultTemplatePostProcessor extends TemplatePostProcessor {

  protected def templateFilter: TemplateFilter

  protected def treeEncodingFormatter: TreeEncodingFormatter

  protected def calcStatistics(treeEncodingWithCount: TreeEncodingWithCount): TemplateStatistic =
    new TemplateStatistic(
      placeholderCount = treeEncodingWithCount.treeEncoding.encodeList.count{
        case Placeholder => true
        case _ => false
      },
      nodeCount = treeEncodingWithCount.treeEncoding.encodeList.size,
      occurrenceCount = treeEncodingWithCount.count
    )

  override def process(treeEncodings: Seq[TreeEncodingWithCount]): Seq[Template] = {
    val templates = treeEncodings.map{ case TreeEncodingWithCount(enc, count) =>
      val (text, compressedEnc) = treeEncodingFormatter.process(enc)
      Template(text, enc, calcStatistics(TreeEncodingWithCount(compressedEnc, count)))
    }
    val gst = new GeneralizedSuffixTree

    def distinct(): Seq[Template] = {
      templates.map(t => (t, t.text.replaceAll("(\\s|\\n|\\t)*", ""))).sortBy(-_._2.length).zipWithIndex.filter { case ((_, text), index) =>
        val matches = gst.search(text)
        if (matches.isEmpty) {
          gst.put(text, index)
          true
        } else {
          false
        }
      }.map(_._1._1)
    }

    val distinctTemplates = distinct()
    distinctTemplates.filter(templateFilter.isPossibleTemplate)
  }


}

