package finavia

import scalafx.scene.layout.{VBox, HBox, StackPane}
import scalafx.scene.control.Label
import scalafx.geometry.Insets
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

class FlightStatisticsCard() extends VBox {
  style = "-fx-background-color: #C0C0C0;"
  private def createLabelAndRectangle(label: Label, color: Color): (Label, Rectangle) = {
    val rectangle = new Rectangle {
      fill = color
      arcWidth = 15
      arcHeight = 15
    }
    rectangle.width <== label.width
    rectangle.height <== label.height
    (label, rectangle)
  }

  private def createStack(label: Label, rectangle: Rectangle) = new StackPane {
    children = Seq(rectangle, label)
    padding = Insets(5)
  }

  private val labelsAndRectangles = List(
    ("Total flights", Color.White),
    ("Maximum delay", Color.Yellow),
    ("Minimum delay", Color.Yellow),
    ("Maximum early", Color.Green),
    ("Minimum early", Color.Green),
    ("Average delay", Color.Yellow),
    ("Average early", Color.Green),
    ("Most popular destination", Color.Cyan),
    ("Most delayed destination", Color.Cyan)
  ).map { case (text, color) =>
    val label = new Label(text)
    val rectangle = createLabelAndRectangle(label, color)._2
    (label, rectangle)
  }

  private val stacks = labelsAndRectangles.map { case (label, rectangle) => createStack(label, rectangle) }
  children = stacks
  spacing = 10

  def updateStatistics(flightDataList: List[FlightData]): Unit = {
    val totalFlights = flightDataList.length
    val delays = flightDataList.map(_.deviationInMinutes)
    val delayStats = delays.filter(_ > 0)
    val earlyStats = delays.filter(_ < 0)

    val destinationGroups = flightDataList.groupBy(_.route_n_1)
    val mostPopularDestination = destinationGroups.maxByOption(_._2.length).map(_._1).getOrElse("N/A")

    val destinationDelayAverages = destinationGroups.view.mapValues(flights => {
      val delays = flights.map(_.deviationInMinutes).filter(_ > 0)
      if (delays.nonEmpty) delays.sum.toDouble / delays.length else 0.0
    }).toMap

    val mostDelayedDestination = destinationDelayAverages.maxByOption(_._2).map(_._1).getOrElse("N/A")

    labelsAndRectangles.head._1.text = s"Total flights: $totalFlights"
    labelsAndRectangles(1)._1.text = s"Maximum delay: ${delayStats.maxOption.getOrElse("N/A")}"
    labelsAndRectangles(2)._1.text = s"Minimum delay: ${delayStats.minOption.getOrElse("N/A")}"
    labelsAndRectangles(3)._1.text = s"Maximum early: ${earlyStats.minOption.map(-_).getOrElse("N/A")}"
    labelsAndRectangles(4)._1.text = s"Minimum early: ${earlyStats.maxOption.map(-_).getOrElse("N/A")}"
    labelsAndRectangles(5)._1.text = s"Average delay: ${if (delayStats.nonEmpty) delayStats.sum.toDouble / delayStats.size else "N/A"}"
    labelsAndRectangles(6)._1.text = s"Average early: ${if (earlyStats.nonEmpty) -earlyStats.sum.toDouble / earlyStats.size else "N/A"}"
    labelsAndRectangles(7)._1.text = s"Most popular destination: $mostPopularDestination"
    labelsAndRectangles(8)._1.text = s"Most delayed destination: $mostDelayedDestination"
  }
}
