package scala.edu.jetbrains.plugin.lt.finder

class StatisticIntParameter {
  private var count: Int = 0
  private var min: Int = Int.MaxValue
  private var max: Int = Int.MinValue
  private var curSum: Long = 0
  private var curSquaredSum: Long = 0

  def +=(value: Int): Unit = {
    count += 1
    min = value min min
    max = value max max
    curSum += value
    curSquaredSum += (value * value)
  }

  def getCount: Int = count

  def getMin: Int = min

  def getMax: Int = max

  override def toString: String =
    s"average: ${"%.3f".format(getAverage)}, " +
      s"variance: ${"%.3f".format(getDeviation)}, " +
      s"min: $min, " +
      s"max: $max"

  def getAverage: Double = curSum / count.toDouble

  def getDeviation: Double = Math.sqrt((curSquaredSum - ((curSum * curSum) / count.toDouble)) / count.toDouble)
}
