package hr.flowable.smoketrack.blu

import java.time.LocalDateTime

class TimestampParser(private val clock: Clock) {

    fun totalCigarettesOnDayCounter(utcTimestamps: List<Long>): Int {
        val dates: List<LocalDateTime> = utcTimestamps.map { it.asLocalDateTime() }
        val today = clock.today()
        println("today $today")

        dates.forEach { println("date $it") }
        return dates.count {
            (it.toLocalDate() == today &&
                    it.hour >= 6) || (it.toLocalDate() == today.plusDays(1) && it.hour < 6)
        }
    }

    fun lastDate(utcTimestamps: List<Long>): LocalDateTime =
        utcTimestamps.maxOrNull()?.asLocalDateTime() ?: LocalDateTime.now()
}
