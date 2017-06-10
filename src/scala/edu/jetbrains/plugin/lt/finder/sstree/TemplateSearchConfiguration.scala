package scala.edu.jetbrains.plugin.lt.finder.sstree

import scala.edu.jetbrains.plugin.lt.finder.common.Template
import TemplateSearchConfiguration._

/**
  * Contains functions and predicates
  * for building and filtering possible templates
  */
trait TemplateSearchConfiguration {
  def lengthMinimum: Int
  def lengthMaximum: Int
  def placeholderMaximum: Int
  def nodesMinimum: Int
  def nodesMaximum: Int
  def maxPlaceholderToNodeRatio: Double

  override def toString: String = s"TemplateSearchConfiguration:\n" +
    s"lengthMinimum: $lengthMinimum,\n" +
    s"lengthMaximum: $lengthMaximum,\n" +
    s"placeholderMaximum: $placeholderMaximum,\n" +
    s"nodesMinimum: $nodesMinimum,\n" +
    s"nodesMaximum: $nodesMaximum,\n" +
    s"maxPlaceholderToNodeRatio: $maxPlaceholderToNodeRatio"
}

case class TemplateSearchConfigurationImpl(lengthMinimum: Int,
                                            lengthMaximum: Int,
                                            placeholderMaximum: Int,
                                            nodesMinimum: Int,
                                            nodesMaximum: Int,
                                            maxPlaceholderToNodeRatio: Double
                                          ) extends TemplateSearchConfiguration

object TemplateSearchConfiguration {

  def merge(self: TemplateSearchConfiguration, other: TemplateSearchConfiguration): TemplateSearchConfiguration = {
    new TemplateSearchConfiguration() {
      override val lengthMaximum: Int = self.lengthMaximum + (α * (other.lengthMaximum - self.lengthMaximum)).toInt
      override val lengthMinimum: Int = self.lengthMinimum + (α * (other.lengthMinimum - self.lengthMinimum)).toInt
      override val maxPlaceholderToNodeRatio: Double = self.maxPlaceholderToNodeRatio + α * (other.maxPlaceholderToNodeRatio - self.maxPlaceholderToNodeRatio)
      override val nodesMaximum: Int = self.nodesMaximum + (α * (other.nodesMaximum - self.nodesMaximum)).toInt
      override val nodesMinimum: Int = self.nodesMinimum + (α * (other.nodesMinimum - self.nodesMinimum)).toInt
      override val placeholderMaximum: Int = self.placeholderMaximum + (α * (other.placeholderMaximum - self.placeholderMaximum)).toInt
    }
  }

  val α = 0.5
}

object DefaultSearchConfiguration extends TemplateSearchConfiguration {
    val lengthMinimum = 15
    val lengthMaximum = 100
    val placeholderMaximum = 5
    val nodesMinimum = 3
    val nodesMaximum = 50
    val maxPlaceholderToNodeRatio = 0.33

//    val lengthMinimum = 30
//    val lengthMaximum = 3000
//    val placeholderMaximum = 30
//    val nodesMinimum = 3
//    val nodesMaximum = 3000
//    val maxPlaceholderToNodeRatio = 0.99
}

class TemplateFilter(templateSearchConfiguration: TemplateSearchConfiguration) {
  import templateSearchConfiguration._

  def isPossibleTemplate(template: Template): Boolean =
    template.templateStatistic.placeholderCount <= placeholderMaximum &&
      template.templateStatistic.placeholderToNodeRatio <= maxPlaceholderToNodeRatio &&
      template.templateStatistic.nodeCount >= nodesMinimum &&
      template.templateStatistic.nodeCount <= nodesMaximum &&
      template.text.length >= lengthMinimum &&
      template.text.length <= lengthMaximum
}

