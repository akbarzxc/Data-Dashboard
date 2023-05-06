package finavia
import scalafx.scene.control.Label
import scalafx.stage.{FileChooser, Stage}
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import scala.xml.{Elem, PrettyPrinter}

class DashboardController(
    timestampLabel: Label,
    pieChart: FlightStatusPieChart,
    scatterChart: FlightStatusScatterChart,
    topAircraftTypesChartData: AircraftsBarChart,
    topDestinationsChartData: DestinationsBarChart,
    flightStatisticsCard: FlightStatisticsCard,
    stage: Stage) {
  private var flightDataList: List[FlightData] = List.empty

  def updateTimestamp(xmlstring: String): Unit = {
    timestampLabel.text = s"LAST!!Data updated at: ${FinaviaDataParser.extractUpdateTime(xmlstring)}"
  }

  def reloadData(): Unit = {
    val xmlString = FlightDataFetcher.fetchFlightDataXml()
    loadDataFromXml(xmlString)
  }

  def loadDataFromXml(xmlString: String): Unit = {
    flightDataList = FinaviaDataParser.parseFlightData(xmlString).toList
    pieChart.updateData(flightDataList)
    scatterChart.updateData(flightDataList)
    topAircraftTypesChartData.updateData(flightDataList)
    topDestinationsChartData.updateData(flightDataList)
    updateTimestamp(xmlString)
    flightStatisticsCard.updateStatistics(flightDataList)
  }

  def saveDashboardData(): Unit = {
    val fileChooser = new FileChooser()
    fileChooser.extensionFilters.addAll(
      new FileChooser.ExtensionFilter("XML Files", "*.xml")
    )
    val selectedFile = fileChooser.showSaveDialog(stage)
    if (selectedFile != null) {
      val xmlData = dataToXml(flightDataList)
      val xmlString = new PrettyPrinter(120, 2).format(xmlData)
      Files.write(selectedFile.toPath, xmlString.getBytes(StandardCharsets.UTF_8))
    }
  }
  def dataToXml(data: List[FlightData]): Elem = {
    <flights>
      {data.map { flight =>
      <flight>
        <h_apt>{flight.h_apt}</h_apt>
        <actype>{flight.actype}</actype>
        <route_n_1>{flight.route_n_1}</route_n_1>
        <prt>{flight.prt}</prt>
        <sdt>{flight.sdt}</sdt>
        <act_d>{flight.act_d}</act_d>
      </flight>
      }}
    </flights>
  }
}
