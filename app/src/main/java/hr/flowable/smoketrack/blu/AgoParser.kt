package hr.flowable.smoketrack.blu

import java.time.Duration

class AgoParser {

    fun asAgo(duration: Duration): String =
        when {
            duration.toDays() > 365 -> "More than a year"
            duration.toDays() != 0L -> "${duration.toDays()} day${if (duration.toDays() > 1) "s" else ""}"
            duration.toHours() != 0L -> "${duration.toHours()}h${
                if (duration.minusHours(duration.toHours()).toMinutes() != 0L) " and ${duration.minusHours(duration.toHours()).toMinutes()}m"
                else ""
            }"
            duration.toMinutes() != 0L -> "${duration.toMinutes()}m"
            else -> "a few seconds"
        }
}
