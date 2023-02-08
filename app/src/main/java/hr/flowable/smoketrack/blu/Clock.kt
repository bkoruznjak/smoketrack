package hr.flowable.smoketrack.blu

import java.time.LocalDate

class Clock {

    fun now() = System.currentTimeMillis()

    fun today(): LocalDate = now().asLocalDateTime().let { localDateTime ->
        // if the time is 5:59:59:9999 it should go one day back
        // because we want to monitor from 6am to 6am
        if (localDateTime.hour < 6) localDateTime.minusDays(1).toLocalDate()
        else localDateTime.toLocalDate()
    }
}
