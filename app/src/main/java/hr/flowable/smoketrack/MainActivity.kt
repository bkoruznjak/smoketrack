package hr.flowable.smoketrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hr.flowable.smoketrack.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

private const val isProd = false

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  private val database by lazy { FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL) }
  private val table by lazy { if (isProd) BuildConfig.TABLE_PROD else BuildConfig.TABLE_TEST }
  private var lastCigaretteSmokedOn: LocalDateTime? = null

  private lateinit var clockTick: Job

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

    binding.buttonRecordSmoke.setOnClickListener {
      recordSmoke()
    }

    observeSmokes()

    clockTick = CoroutineScope(Dispatchers.IO).launch {
      while (true) {
        delay(60 * 1000)
        withContext(Dispatchers.Main) {
          renderLastSmokedTime()
        }
      }
    }
  }

  override fun onDestroy() {
    clockTick.cancel()
    super.onDestroy()
  }

  private fun observeSmokes() {
    with(database) {
      val ref = getReference(table)
      ref.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          val dates: List<LocalDateTime> = snapshot.asDateTimes()
          val today = System.currentTimeMillis().asLocalDateTime().toLocalDate()
          binding.textTotalCigarettesDay.text =
            getString(R.string.text_total_cigarettes_per_day, dates.count {
              (it.toLocalDate() == today &&
                it.hour >= 6) || (it.toLocalDate() == today.plusDays(1) && it.hour < 6)
            })
          lastCigaretteSmokedOn = dates.lastOrNull()
          renderLastSmokedTime()
        }

        override fun onCancelled(error: DatabaseError) = Unit

      })
    }
  }

  private fun renderLastSmokedTime(nowMillis: Long = System.currentTimeMillis()) {
    binding.textLastCigaretteSmokedAgo.text =
      getString(R.string.text_last_cigarette_smoked_ago, lastCigaretteSmokedOn?.let { date ->
        val now = nowMillis.asLocalDateTime()
        Duration.between(date, now).asAgo()
      } ?: "%")
  }

  private fun recordSmoke() {
    binding.buttonRecordSmoke.isEnabled = false
    with(database) {
      val ref = getReference(table)
      ref.get().addOnSuccessListener { data ->
        val timestamps: List<Long> = data.children.map { it.value as? Long }.filterNotNull()
        val updated = timestamps + listOf(System.currentTimeMillis())
        ref.setValue(updated)
        binding.buttonRecordSmoke.isEnabled = true
      }
    }
  }
}

fun Duration.asAgo(): String =
  when {
    toDays() > 365    -> "More than a year"
    toDays() != 0L    -> "${toDays()} day${if (toDays() > 1) "s" else ""}"
    toHours() != 0L   -> "${toHours()}h${
      if (minusHours(toHours()).toMinutes() != 0L) " and ${minusHours(toHours()).toMinutes()}m"
      else ""
    }"
    toMinutes() != 0L -> "${toMinutes()}m"
    else              -> "a few seconds"
  }

private fun DataSnapshot.asDateTimes() =
  children.map { it.value as? Long }.filterNotNull().map { it.asLocalDateTime() }

private fun Long.asLocalDateTime() =
  LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)
