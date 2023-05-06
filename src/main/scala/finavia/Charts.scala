package finavia

import scalafx.collections.ObservableBuffer
import scalafx.scene.chart.{BarChart, CategoryAxis, NumberAxis, PieChart, ScatterChart, XYChart}
import scalafx.scene.layout.VBox
import scalafx.geometry.Side
import scalafx.scene.control.Tooltip
import scalafx.Includes.jfxXYChartData2sfx
import scalafx.Includes.jfxPieChartData2sfx
import scalafx.Includes.observableList2ObservableBuffer
import scalafx.scene.control.Tooltip
import scala.collection.JavaConverters._
class FlightStatusPieChart(var flightDataList: List[FlightData]) extends VBox {
  private def createPieChart(): PieChart = {
    // Count the occurrences of each flight status, handle missing information
    val statusCounts = flightDataList.groupBy { flightData =>
      if (flightData.prt == null || flightData.prt.isEmpty) "TBA"
      else flightData.prt
    }.view.mapValues(_.size).toMap

    new PieChart {
      data = statusCounts.map { case (status, count) =>
        val pieChartData = PieChart.Data(status, count)
        pieChartData
      }.toSeq
      clockwise = false
      title = "FLIGHT STATUS"
    }
  }

  private val pieChart = createPieChart()
  children = pieChart

  // Add tooltips to pie chart slices
  pieChart.data().foreach { pieChartData =>
    val status = pieChartData.name.value
    val count = pieChartData.pieValue.value
    val tooltip = Tooltip(s"Status: $status\nCount: $count")
    Tooltip.install(pieChartData.node.value, tooltip)
  }


  def updateData(newFlightDataList: List[FlightData]): Unit = {
    flightDataList = newFlightDataList
    val newStatusCounts = flightDataList.groupBy { flightData =>
      if (flightData.prt == null || flightData.prt.isEmpty) "TBA"
      else flightData.prt
    }.view.mapValues(_.size).toMap

    pieChart.data = newStatusCounts.map { case (status, count) =>
      val pieChartData = PieChart.Data(status, count)
      pieChartData
    }.toSeq

    // Update tooltips for the updated pie chart slices
    pieChart.data().foreach { pieChartData =>
      val status = pieChartData.name.value
      val count = pieChartData.pieValue.value
      val tooltip = Tooltip(s"Status: $status\nCount: $count")
      Tooltip.install(pieChartData.node.value, tooltip)
    }
  }
}



class FlightStatusScatterChart(var flightDataList: List[FlightData]) extends VBox {

  private val xAxis: CategoryAxis = new CategoryAxis {

  }

  private def createScatterChart(): ScatterChart[String, Number] = {
    val delays = flightDataList.filter(flight => flight.deviationStatus == "Delay" && flight.deviationInMinutes != 0).map(flight => (flight.route_n_1, flight.deviationInMinutes.toDouble))
    val earlyFlights = flightDataList.filter(flight => flight.deviationStatus == "Early" && flight.deviationInMinutes != 0).map(flight => (flight.route_n_1, flight.deviationInMinutes.toDouble))

    val destinationsWithNonZeroDeviation = (delays ++ earlyFlights).map(_._1).distinct
    xAxis.categories = ObservableBuffer.from(destinationsWithNonZeroDeviation)

    new ScatterChart(xAxis, NumberAxis("Deviation in Minutes", -120, 120, 30)) {
      title = "Scatter Chart"
      legendSide = Side.Bottom
      data = ObservableBuffer(
        xySeries("Delays", delays),
        xySeries("Early Flights", earlyFlights)
      )
    }
  }

  private def xySeries(name: String, data: Seq[(String, Double)]) = XYChart.Series[String, Number](
    name,
    ObservableBuffer.from(data.map({ case (x, y) =>
      val dataPoint = XYChart.Data[String, Number](x, y)
      val tooltip = Tooltip(s"Destination: $x\nDeviation: $y")
      Tooltip.install(dataPoint.node.value, tooltip)
      dataPoint
    }))
  )

  private val scatterChart = createScatterChart()
  children = scatterChart

  def updateData(newFlightDataList: List[FlightData]): Unit = {
    flightDataList = newFlightDataList
    val newDelays = flightDataList.filter(flight => flight.deviationStatus == "Delay" && flight.deviationInMinutes != 0).map(flight => (flight.route_n_1, flight.deviationInMinutes.toDouble))
    val newEarlyFlights = flightDataList.filter(flight => flight.deviationStatus == "Early" && flight.deviationInMinutes != 0).map(flight => (flight.route_n_1, flight.deviationInMinutes.toDouble))
    val newDestinationsWithNonZeroDeviation = (newDelays ++ newEarlyFlights).map(_._1).distinct

    xAxis.categories = ObservableBuffer.from(newDestinationsWithNonZeroDeviation)
    scatterChart.data = ObservableBuffer(
      xySeries("Delays", newDelays),
      xySeries("Early Flights", newEarlyFlights)
    )
  }

}


class DestinationsBarChart(var flightDataList: List[FlightData])
    extends VBox {

  // Calculate the top 10 destinations
  val destinationCounts = flightDataList.groupBy(_.route_n_1).view.mapValues(_.size).toMap.toList
  val topDestinations = destinationCounts.sortBy(-_._2).take(10)
  val topDestinationNames = topDestinations.map(_._1)
  val topDestinationCounts = topDestinations.map(_._2)

  // Create the BarChart and set its properties
  val chart = new BarChart[String, Number](new CategoryAxis(), new NumberAxis()) {
    legendVisible = false
    title = "Top 10 destinations"
  }


// Add the data to the chart
  val seriesData = ObservableBuffer(topDestinationNames zip topDestinationCounts map (xy => XYChart.Data(xy._1, xy._2.asInstanceOf[Number])): _*)
  val series = new XYChart.Series[String, Number] {
    name = "Data"
    data = seriesData
  }
  chart.data.get.add(series)

  // Add tooltips to the bars in the bar chart
  seriesData.foreach { barData =>
    val destination = barData.getXValue
    val count = barData.getYValue
    val tooltip = Tooltip(s"Destination: $destination\nCount: $count")
    Tooltip.install(barData.getNode, tooltip)
  }

  children = Seq(chart)

  def updateData(newFlightDataList: List[FlightData]): Unit = {
    flightDataList = newFlightDataList
    val newDestinationCounts = flightDataList.groupBy(_.route_n_1).view.mapValues(_.size).toMap.toList
    val newTopDestinations = newDestinationCounts.sortBy(-_._2).take(10)
    val newTopDestinationNames = newTopDestinations.map(_._1)
    val newTopDestinationCounts = newTopDestinations.map(_._2)

    val newSeriesData = ObservableBuffer(newTopDestinationNames zip newTopDestinationCounts map (xy => XYChart.Data(xy._1, xy._2.asInstanceOf[Number])): _*)
    chart.data.get.clear()
    val newSeries = new XYChart.Series[String, Number] {
      name = "Data"
      data = newSeriesData
    }
    chart.data.get.add(newSeries)

    // Add tooltips to the bars in the bar chart
    newSeriesData.foreach { barData =>
      val destination = barData.getXValue
      val count = barData.getYValue
      val tooltip = Tooltip(s"Destination: $destination\nCount: $count")
      Tooltip.install(barData.getNode, tooltip)
    }
  }

}



class AircraftsBarChart(var flightDataList: List[FlightData])
    extends VBox {

    // Calculate the top 10 aircraft types
  val aircraftTypeCounts = flightDataList.groupBy(_.actype).view.mapValues(_.size).toMap.toList
  val topAircraftTypes = aircraftTypeCounts.sortBy(-_._2).take(10)
  val topAircraftTypeNames = topAircraftTypes.map(_._1)
  val topAircraftTypeCounts = topAircraftTypes.map(_._2)
  // Create the BarChart and set its properties
  val chart = new BarChart[String, Number](new CategoryAxis(), new NumberAxis()) {
    legendVisible = false
    title = "Top 10 aircaft"
  }

  // Add the data to the chart
  val seriesData = ObservableBuffer(topAircraftTypeNames zip topAircraftTypeCounts map (xy => XYChart.Data(xy._1, xy._2.asInstanceOf[Number])): _*)
  val series = new XYChart.Series[String, Number] {
    name = "Data"
    data = seriesData
  }
  chart.data.get.add(series)

  // Add tooltips to the bars in the bar chart
  seriesData.foreach { barData =>
    val aircraftType = barData.getXValue
    val count = barData.getYValue
    val tooltip = Tooltip(s"Aircraft Type: $aircraftType\nCount: $count")
    Tooltip.install(barData.getNode, tooltip)
  }


  children = Seq(chart)

  def updateData(newFlightDataList: List[FlightData]): Unit = {
    flightDataList = newFlightDataList
    val newAircraftTypeCounts = flightDataList.groupBy(_.actype).view.mapValues(_.size).toMap.toList
    val newTopAircraftTypes = newAircraftTypeCounts.sortBy(-_._2).take(10)
    val newTopAircraftTypeNames = newTopAircraftTypes.map(_._1)
    val newTopAircraftTypeCounts = newTopAircraftTypes.map(_._2)

    val newSeriesData = ObservableBuffer(newTopAircraftTypeNames zip newTopAircraftTypeCounts map (xy => XYChart.Data(xy._1, xy._2.asInstanceOf[Number])): _*)
    chart.data.get.clear()
    val newSeries = new XYChart.Series[String, Number] {
      name = "Data"
      data = newSeriesData
    }
    chart.data.get.add(newSeries)

    // Add tooltips to the bars in the bar chart
    newSeriesData.foreach { barData =>
      val aircraftType = barData.getXValue
      val count = barData.getYValue
      val tooltip = Tooltip(s"Aircraft Type: $aircraftType\nCount: $count")
      Tooltip.install(barData.getNode, tooltip)
    }
  }

}