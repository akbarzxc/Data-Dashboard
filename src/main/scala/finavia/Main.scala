package finavia
import scalafx.collections.ObservableBuffer
import scalafx.scene.chart.{CategoryAxis, NumberAxis, PieChart, ScatterChart, XYChart}
import scalafx.application.JFXApp3
import scalafx.scene.control.{Label, MenuBar}
import scalafx.scene.layout.{BorderPane, ColumnConstraints, GridPane, HBox, RowConstraints, StackPane, VBox}
import scalafx.scene.shape.Rectangle
import scalafx.scene.Scene
import scalafx.geometry.Insets

import scala.xml.Elem
object Main extends JFXApp3 {
  override def start(): Unit = {
    val xmlstring = FlightDataFetcher.fetchFlightDataXml()
    val flightDataList: List[FlightData] = FinaviaDataParser.parseFlightData(xmlstring).toList
    val pieChart = new FlightStatusPieChart(flightDataList)
    val scatterChart = new FlightStatusScatterChart(flightDataList)
    val topDestinationsChartData = new DestinationsBarChart(flightDataList)
    val topAircraftTypesChartData = new AircraftsBarChart(flightDataList)
    val timestamp = FinaviaDataParser.extractUpdateTime(xmlstring)
    val flightStatisticsCard = new FlightStatisticsCard()
    flightStatisticsCard.updateStatistics(flightDataList)

    val timestampLabel = new Label {
      text = s"LAST Data updated: $timestamp"
    }
    val dashboardController = new DashboardController(timestampLabel, pieChart, scatterChart, topAircraftTypesChartData, topDestinationsChartData, flightStatisticsCard, stage)
    def updateTimestamp(): Unit = {
      dashboardController.updateTimestamp(xmlstring)
    }

    def reloadData(): Unit = {
      dashboardController.reloadData()
    }

    def loadDataFromXml(xmlString: String): Unit = {
      dashboardController.loadDataFromXml(xmlString)
    }

    def saveDashboardData(): Unit = {
      dashboardController.saveDashboardData()
    }

    def dataToXml(data: List[FlightData]): Elem = {
      dashboardController.dataToXml(data)
    }

    val customMenu = new CustomMenu(loadDataFromXml, saveDashboardData, reloadData, stage)
    val rectangleMenu = new RectangleMenu(pieChart, scatterChart, topDestinationsChartData, topAircraftTypesChartData)
    val chartGridPane = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(10)
      columnConstraints = Seq(new ColumnConstraints, new ColumnConstraints)
      rowConstraints = Seq(new RowConstraints, new RowConstraints)

      add(pieChart, 0, 0)
      add(scatterChart, 1, 0)
      add(topDestinationsChartData, 0, 1)
      add(topAircraftTypesChartData, 1, 1)
    }
    chartGridPane.padding = Insets(10)
    chartGridPane.vgap = 10
    stage = new JFXApp3.PrimaryStage {
      title = "Helsinki Airport Data Dashboard"
      scene = new Scene {
        root = new BorderPane {
          top = customMenu
          left = flightStatisticsCard
          center = chartGridPane
          bottom = timestampLabel
          right = rectangleMenu.menu
        }
      }
    }
  }}
