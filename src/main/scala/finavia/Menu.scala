package finavia
import scalafx.geometry.Insets
import scalafx.scene.control.{Menu, MenuBar, MenuItem, ToggleButton}
import scalafx.scene.layout.VBox
import scalafx.stage.{FileChooser, Stage}
import scala.io.Source
import scalafx.scene.control.{Menu, MenuItem}
import scalafx.event.ActionEvent
import scalafx.Includes._
import scalafx.Includes.eventClosureWrapperWithParam
import scalafx.event.EventIncludes.eventClosureWrapperWithParam

class CustomMenu(
    openXmlFile: String => Unit,
    saveDashboardData: () => Unit,
    reloadData: () => Unit,
    stage: Stage
) extends MenuBar {
  private val openItem = new MenuItem("Open") {
    onAction = _ => {
      val fileChooser = new FileChooser()
      fileChooser.extensionFilters.addAll(
        new FileChooser.ExtensionFilter("XML Files", "*.xml")
      )
      val selectedFile = fileChooser.showOpenDialog(stage)
      if (selectedFile != null) {
        val xmlString = Source.fromFile(selectedFile).getLines().mkString("\n")
        // Update the data in the charts using the xmlString
        openXmlFile(xmlString)
      }
    }
  }

  private val reloadItem = new MenuItem("Reload") {
    onAction = _ => reloadData()
  }

  private val saveItem = new MenuItem("Save") {
    onAction = _ => saveDashboardData()
  }

  private val fileMenu = new Menu("File") {
    items = List(openItem, reloadItem, saveItem)
  }

  menus = List(fileMenu)
}



class RectangleMenu(flightStatusPieChart: FlightStatusPieChart, flightStatusScatterChart: FlightStatusScatterChart,bar: DestinationsBarChart, bar2: AircraftsBarChart) {
 // Create toggle buttons for each chart
  val pieChartButton = new ToggleButton("Flight Status Pie Chart") {
    selected <==> flightStatusPieChart.visible
  }
  val scatterChartButton = new ToggleButton("Delay & Early Flights") {
    selected <==> flightStatusScatterChart.visible
  }
  val barChartButton = new ToggleButton("Top 10 Destinations") {
    selected <==> bar.visible
  }
  val barChartButton2 = new ToggleButton("Top 10 Aircraft types ") {
    selected <==> bar2.visible
  }
  val menu: VBox = new VBox {
    style = "-fx-background-color: #C0C0C0;"
    spacing = 10
    padding = Insets(20)
    children = List(
      new VBox {
        spacing = 10
        children = List(
          pieChartButton,
          scatterChartButton,
          barChartButton,
          barChartButton2)
      }
    )
  }
}
