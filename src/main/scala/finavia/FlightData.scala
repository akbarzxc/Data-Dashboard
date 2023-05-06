package finavia

import java.time.{Duration, LocalDateTime}
import java.time.format.DateTimeFormatter

case class FlightData(h_apt: String, actype: String, route_n_1: String, prt: String, sdt: String, act_d: String) {

  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")


  def deviationInMinutes: Int = {
    if (sdt.isEmpty || act_d.isEmpty) {
      0
    } else {
      val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val scheduledTime = LocalDateTime.parse(sdt, dateTimeFormatter)
      val actualTime = LocalDateTime.parse(act_d, dateTimeFormatter)
      val duration = Duration.between(scheduledTime, actualTime)
      duration.toMinutes.toInt
    }
  }

  def deviationStatus: String = {
    val minutes = deviationInMinutes
    if (minutes < 0) "Delay" else "Early"
  }
}
