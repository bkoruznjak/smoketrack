package hr.flowable.smoketrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hr.flowable.smoketrack.blu.AgoParser
import hr.flowable.smoketrack.blu.Clock
import hr.flowable.smoketrack.blu.TimestampParser
import hr.flowable.smoketrack.blu.asLocalDateTime
import hr.flowable.smoketrack.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalDateTime

private const val isProd = false

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val clock: Clock by lazy { Clock() }
    private val agoParser: AgoParser by lazy { AgoParser() }
    private val timestampParser: TimestampParser by lazy { TimestampParser(clock) }

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
                    val utcTimestamps = snapshot.asTimestamps()
                    val totalCountOnDay = timestampParser.totalCigarettesOnDayCounter(utcTimestamps)
                    val lastKnownDate = timestampParser.lastDate(utcTimestamps)
                    binding.textTotalCigarettesDay.text =
                        getString(R.string.text_total_cigarettes_per_day, totalCountOnDay)
                    lastCigaretteSmokedOn = lastKnownDate
                    renderLastSmokedTime()
                }

                override fun onCancelled(error: DatabaseError) = Unit

            })
        }
    }

    private fun renderLastSmokedTime(nowMillis: Long = clock.now()) {
        binding.textLastCigaretteSmokedAgo.text =
            getString(R.string.text_last_cigarette_smoked_ago, lastCigaretteSmokedOn?.let { date ->
                val now = nowMillis.asLocalDateTime()
                agoParser.asAgo(Duration.between(date, now))
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

private fun DataSnapshot.asTimestamps() = children.mapNotNull { it.value as? Long }
