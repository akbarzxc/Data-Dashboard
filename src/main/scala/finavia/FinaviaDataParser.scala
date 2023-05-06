package finavia

import scala.xml.XML

object FinaviaDataParser {

  def parseFlightData(xmlString: String): Seq[FlightData] = {
    val xml = XML.loadString(xmlString)

    val allFlights = (xml \\ "flight").map { flightNode =>
      FlightData(
        (flightNode \ "h_apt").text,
        (flightNode \ "actype").text,
        (flightNode \ "route_n_1").text,
        (flightNode \ "prt").text,
        (flightNode \ "sdt").text,
        (flightNode \ "act_d").text
      )
    }
    
    val filteredFlights = allFlights.filter(flight => flight.h_apt == "HEL")
    filteredFlights
  }
  def extractUpdateTime(xmlString: String): String = {
    val xml = XML.loadString(xmlString)
    (xml \\ "timestamp").text
  }
}
