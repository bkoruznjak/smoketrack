package hr.flowable.smoketrack

import hr.flowable.smoketrack.blu.Clock
import hr.flowable.smoketrack.blu.TimestampParser
import hr.flowable.smoketrack.blu.asLocalDateTime
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import org.junit.Test

class TimestampParserTest {

    private val testNow = 1675721690979L //2023-02-06

    private val testTimeStamps = listOf(
        1675663198052L, //2023-02-06T06:13:58.052
        1675664038052L, //2023-02-06T06:13:58.052
        1675721690979L, //2023-02-06T06:13:58.052
        1675749542899L, //2023-02-06T06:13:58.052
        1675749613899L, //2023-02-06T06:13:58.052
    )

    private val clock: Clock = mockk {
        every { now() } returns testNow
        every { today() } returns testNow.asLocalDateTime().let { localDateTime ->
            // if the time is 5:59:59:9999 it should go one day back
            // because we want to monitor from 6am to 6am
            if (localDateTime.hour < 6) localDateTime.minusDays(1).toLocalDate()
            else localDateTime.toLocalDate()
        }
    }
    private val underTest = TimestampParser(clock)

    @Test
    fun `displays correct count from 6 AM to 6 AM`() {
        val actualTotalCount = underTest.totalCigarettesOnDayCounter(testTimeStamps)

        val expectedTotalCount = 3

        assertEquals(expectedTotalCount, actualTotalCount)
    }
}
