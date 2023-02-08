package hr.flowable.smoketrack

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

class AsNowTest {

  @Test
  fun `displays more than a Year ago correctly`() {
    val actual = Duration.ofDays(366)

    val actualFormatted = actual.asAgo()

    assertEquals("More than a year", actualFormatted)
  }

  @Test
  fun `displays days ago correctly`() {
    val actual = Duration.ofDays(1)

    val actualFormatted = actual.asAgo()

    assertEquals("1 day", actualFormatted)

    val actualPlural = Duration.ofDays(365)

    val actualPluralFormatted = actualPlural.asAgo()

    assertEquals("365 days", actualPluralFormatted)
  }

  @Test
  fun `displays hours and minutes ago correctly`() {
    val actualSingle = Duration.ofMinutes(60)

    val actualSingleFormatted = actualSingle.asAgo()

    assertEquals("1h", actualSingleFormatted)

    val actual = Duration.ofMinutes(61)

    val actualFormatted = actual.asAgo()

    assertEquals("1h and 1m", actualFormatted)
  }

  @Test
  fun `displays minutes ago correctly`() {
    val actualSingle = Duration.ofMinutes(59)

    val actualSingleFormatted = actualSingle.asAgo()

    assertEquals("59m", actualSingleFormatted)
  }

  @Test
  fun `displays a few seconds ago correctly`() {
    val actualSingle = Duration.ofSeconds(59)

    val actualSingleFormatted = actualSingle.asAgo()

    assertEquals("a few seconds", actualSingleFormatted)
  }
}
