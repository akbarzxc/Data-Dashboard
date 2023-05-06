package finavia

import sttp.client3._
import ApiKeys.keys

object FlightDataFetcher {

  val appId = keys("your_app_id_here")
  val appKey = keys("your_app_key_here")
  val url = "https://api.finavia.fi/flights/public/v0/flights/all/all"

  val request = basicRequest.get(uri"$url")
    .header("Accept", "application/xml")
    .header("app_id", appId)
    .header("app_key", appKey)

  val backend = HttpClientSyncBackend()

  def fetchFlightDataXml(): String = {
    val response: Identity[Response[Either[String, String]]] = request.send(backend)
    response.body match {
      case Right(xmlString) => xmlString
      case Left(errorMessage) => throw new RuntimeException(s"Error fetching flight data: $errorMessage")
    }
  }


}
