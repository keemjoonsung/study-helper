package com.skku.studyhelper

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.media.Image
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.zip.Inflater


class HomeActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: Editor
    private lateinit var minutes : String
    private val timerScope = CoroutineScope(Dispatchers.Main)
    private lateinit var textViewTimer : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.apply()
        textViewTimer = findViewById(R.id.textViewTimer)
        if(sharedPreferences.getBoolean("isTimer",false)){ //타이머가 켜져있으면
            Log.d("타이머 켜짐", "d")
            val startTime = sharedPreferences.getLong("startTime", 0)
            val currentTime = System.currentTimeMillis()
            val elapsedMillis = currentTime - startTime

            val initialMinutes = sharedPreferences.getString("minutes", "0")?.toInt() ?: 0
            val initialTotalSeconds = initialMinutes * 60

            val elapsedSeconds = elapsedMillis / 1000
            val remainingSeconds = initialTotalSeconds - elapsedSeconds.toInt()
            if(remainingSeconds < 0){ //타이머 다끝났으면 종료
                textViewTimer.text = "00:00"
                editor.putBoolean("isTimer",false)
                editor.putLong("startTime", 0)
                editor.putInt("gold", sharedPreferences.getInt("gold", 0) + sharedPreferences.getInt("reward",0))
                editor.putInt("reward",0)
                editor.apply()
            }else { //아직 타이머 안끝났을때
                val remainingMinutesPart = remainingSeconds / 60
                val remainingSecondsPart = remainingSeconds % 60
                startTimer(String.format("%02d:%02d", remainingMinutesPart, remainingSecondsPart))
                editor.putString("timer",String.format("%02d:%02d", remainingMinutesPart, remainingSecondsPart))
            }
        }
        textViewTimer.setText(sharedPreferences.getString("timer" , "null"))

        val textViewID : TextView = findViewById(R.id.textViewID)
        textViewID.setText(sharedPreferences.getString("id","SKKU"))
        minutes = sharedPreferences.getString("timer", "00:00").toString()
        //startTimer(minutes)
        val imageViewProfile : ImageView = findViewById(R.id.imageViewProfile)

        Animal.animalMap[sharedPreferences.getString("profile", "null")]?.let {
            imageViewProfile.setImageResource(
                it
            )

        }
        findViewById<TextView>(R.id.textViewAnimalName).setText(
            Animal.animalNameMap2[sharedPreferences.getString("profile","null")]
        )
        findViewById<TextView>(R.id.textViewAnimalGold).setText(
            "1분당 " + Animal.animalGoldMap[sharedPreferences.getString("profile","null")].toString()
        )
        val btnShop = findViewById<ImageView>(R.id.imageViewShop)
        val intentShop = Intent(this, ShopActivity::class.java)
        btnShop.setOnClickListener {
            startActivity(intentShop)
            finish()
        }
        val btnTimer = findViewById<ImageView>(R.id.imageViewTimer)
        btnTimer.setOnClickListener{
            if(!sharedPreferences.getBoolean("isTimer",false)) {
                showStopwatchDialog()
            }
        }
        val btnSetting = findViewById<ImageView>(R.id.imageViewSetting)

        btnSetting.setOnClickListener{
            if(sharedPreferences.getBoolean("isTimer",false)){
                showRejectDialog()
            }else {
                showSettingDialog()
            }
        }

    }

    @SuppressLint("MissingInflatedId")
    private fun showRejectDialog(){
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.reject_dialog,null)
        dialog.setContentView(view)

        view.findViewById<Button>(R.id.buttonOkay).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun startTimer(minutes : String) {
        if(minutes == "00:00") return
        val parts = minutes.split(":")
        var totalSeconds = parts[0].toInt() * 60 + parts[1].toInt()

        timerScope.launch {
            while (totalSeconds > 0) {
                delay(1000)
                totalSeconds--

                val minutesPart = totalSeconds / 60
                val secondsPart = totalSeconds % 60
                val timer = String.format("%02d:%02d", minutesPart, secondsPart)
                textViewTimer.text = timer
                editor.putString("timer",timer)
                editor.apply()
            }
            textViewTimer.text = "00:00"
            editor.putBoolean("isTimer",false)
            editor.putLong("startTime", 0)
            editor.putInt("gold", sharedPreferences.getInt("gold", 0) + sharedPreferences.getInt("reward",0))
            editor.putInt("reward",0)
            editor.apply()
        }
    }

    private fun showStopwatchDialog() {
        val dialog = Dialog(this)

        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.timer_dialog, null)
        dialog.setContentView(dialogView)

        val editTextMinutes: EditText = dialogView.findViewById(R.id.editTextMinutes)
        val buttonSet: Button = dialogView.findViewById(R.id.buttonSet)

        buttonSet.setOnClickListener {
            val minutes = editTextMinutes.text.toString()
            if(minutes == "") {
                Toast.makeText(this, "시간은 1분이상, 60분 이하로 설정해주세요",Toast.LENGTH_SHORT).show()
            }else {
                val num_min = minutes.toInt()
                if (num_min <= 0 || num_min > 60) {
                    Toast.makeText(this, "시간은 1분이상, 60분 이하로 설정해주세요", Toast.LENGTH_SHORT).show()
                }else {
                    dialog.dismiss()
                    showConfirmTimerDialog(minutes, sharedPreferences, editor)

                }
            }

        }

        dialog.show()
    }
    private fun showSettingDialog(){
        val dialog = Dialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.setting_dialog,null)
        dialog.setContentView(dialogView)
        val items = ArrayList<String>()

        if (sharedPreferences.getBoolean("frog", false)) {
            items.add("개구리")
        }
        if (sharedPreferences.getBoolean("koala", false)) {
            items.add("코알라")
        }
        if (sharedPreferences.getBoolean("panda", false)) {
            items.add("판다")
        }
        dialogView.findViewById<Button>(R.id.buttonBack).setOnClickListener {
            dialog.dismiss()
        }

        val listViewAdapter = ListViewAdapter(this, items)
        val listView : ListView = dialogView.findViewById(R.id.characterListView)
        listView.adapter = listViewAdapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val name : String =  parent.adapter.getItem(position) as String
            val nameEng = Animal.animalNameMap3[name]
            val profileSrc : Int? = Animal.animalMap[nameEng]

            val profileAnimalGold  = "1분당 " + Animal.animalGoldMap[nameEng].toString()

            val imageViewProfile : PixelatedImageView = findViewById(R.id.imageViewProfile)
            if (profileSrc != null) {
                imageViewProfile.setImageResource(profileSrc)
            }

            val textViewProfile : TextView = findViewById(R.id.textViewAnimalName)
            textViewProfile.setText(name)

            val textViewProfileGold : TextView = findViewById(R.id.textViewAnimalGold)
            textViewProfileGold.setText(profileAnimalGold)
            editor.putString("profile",nameEng)
            editor.apply()
            dialog.dismiss()

        }
        dialog.show()
    }

    private fun showConfirmTimerDialog(minutes: String , sharedPreferences: SharedPreferences, editor: Editor) {
        val dialog = Dialog(this)

        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.timer_confirm, null)
        dialog.setContentView(dialogView)
        val gold = Animal.animalGoldMap[sharedPreferences.getString("profile","null")]?.times(
            minutes.toInt()
        )
        val textViewMinutes: TextView = dialogView.findViewById(R.id.textViewMinutes)
        val btnStartTimer: Button = dialogView.findViewById(R.id.buttonStartTimer)
        val btnCancleTimer: Button = dialogView.findViewById(R.id.buttonCancelTimer)
        val textViewTimer : TextView = findViewById(R.id.textViewTimer)
        textViewMinutes.text = "${minutes}분입니다 (" + gold +"G 획득)"

        btnStartTimer.setOnClickListener {
            dialog.dismiss()
            if (gold != null) {
                editor.putInt("reward",gold)
            }
            editor.putBoolean("isTimer",true)
            editor.putString("minutes",minutes)
            editor.putLong("startTime",System.currentTimeMillis())
            editor.apply()
            textViewTimer.setText(minutes + ":00")
            startTimer(  minutes + ":00")
        }
        btnCancleTimer.setOnClickListener {
            dialog.dismiss()
            showStopwatchDialog()
        }

        dialog.show()
    }

}