package com.example.myapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.myapp.worker.AlarmReceiver
import java.util.Calendar

class SettingsActivity : BaseActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var btnSchedule: Button
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val themeRadioGroup = findViewById<RadioGroup>(R.id.radioGroupTheme)
        val langRadioGroup = findViewById<RadioGroup>(R.id.radioGroupLanguage)
        val btnApply = findViewById<Button>(R.id.btnApply)

        timePicker = findViewById(R.id.timePicker)
        btnSchedule = findViewById(R.id.btnScheduleNotification)
        tvStatus = findViewById(R.id.tvNotificationStatus)

        timePicker.setIs24HourView(true)


        showScheduledTime()

        btnSchedule.setOnClickListener {
            scheduleNotification()
        }


        when (ThemeManager.getThemeMode(this)) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> themeRadioGroup.check(R.id.radioSystem)
            AppCompatDelegate.MODE_NIGHT_NO -> themeRadioGroup.check(R.id.radioLight)
            AppCompatDelegate.MODE_NIGHT_YES -> themeRadioGroup.check(R.id.radioDark)
        }

        when (LanguageManager.getLanguage(this)) {
            "en" -> langRadioGroup.check(R.id.radioEnglish)
            "ru" -> langRadioGroup.check(R.id.radioRussian)
        }

        btnApply.setOnClickListener {
            val selectedTheme = when (themeRadioGroup.checkedRadioButtonId) {
                R.id.radioLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.radioDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            ThemeManager.saveThemeMode(this, selectedTheme)
            AppCompatDelegate.setDefaultNightMode(selectedTheme)

            val selectedLang = when (langRadioGroup.checkedRadioButtonId) {
                R.id.radioRussian -> "ru"
                else -> "en"
            }
            LanguageManager.saveLanguage(this, selectedLang)

            recreate()
        }
    }


    private fun showScheduledTime() {
        val prefs = getSharedPreferences("notification_prefs", MODE_PRIVATE)
        val hour = prefs.getInt("notify_hour", -1)
        val minute = prefs.getInt("notify_minute", -1)
        if (hour != -1 && minute != -1) {
            tvStatus.text = "Scheduled daily at $hour:$minute"
        } else {
            tvStatus.text = "Not scheduled"
        }
    }

    private fun scheduleNotification() {
        val hour = if (Build.VERSION.SDK_INT >= 23) {
            timePicker.hour
        } else {
            timePicker.currentHour
        }
        val minute = if (Build.VERSION.SDK_INT >= 23) {
            timePicker.minute
        } else {
            timePicker.currentMinute
        }

        // Сохраняем выбранное время
        val prefs = getSharedPreferences("notification_prefs", MODE_PRIVATE)
        prefs.edit().putInt("notify_hour", hour).putInt("notify_minute", minute).apply()

        // Проверяем разрешения
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
                Toast.makeText(this, "Please grant notification permission", Toast.LENGTH_LONG).show()
                return
            }
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Для Android 12+ нужна проверка точных будильников
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Please allow exact alarms in settings", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return
            }
        }

        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1) // если время уже прошло сегодня, ставим на завтра
            }
        }

        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            tvStatus.text = "Scheduled daily at $hour:$minute"
            Toast.makeText(this, "Notification scheduled", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, "Failed to schedule: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            scheduleNotification()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}