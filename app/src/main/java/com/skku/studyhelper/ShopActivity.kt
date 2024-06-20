package com.skku.studyhelper

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import org.w3c.dom.Text
import kotlin.properties.Delegates

class ShopActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shop)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.apply()
        var gold : Int = sharedPreferences.getInt("gold", 0)
        val animalList = listOf("frog", "koala", "panda")
        val textViewGold : TextView = findViewById(R.id.textViewGold)
        textViewGold.text = "보유: " + gold.toString()
        val viewPager : ViewPager2 = findViewById(R.id.viewPager)
        val viewPagerAdapter = ViewPagerAdapter(animalList)
        viewPager.adapter = viewPagerAdapter
        val textViewExplain : TextView = findViewById(R.id.textViewExplain)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                textViewExplain.text = Animal.animalExplainMap[position]
            }
        })
        val btnBuy : Button = findViewById(R.id.buttonBuy)
        btnBuy.setOnClickListener {
            val position = viewPager.currentItem
            val price = Animal.animalPriceMap[position]
            if(sharedPreferences.getBoolean(Animal.animalNameMap[position],false)){ //이미 있으면
                //이미 존재한다고 토스트 띄움
                Toast.makeText(this, "이미 구입한 동물입니다.",Toast.LENGTH_SHORT).show()
            }else{
                if (price != null) {
                    if(price <= gold) { //살수 있음
                        val dialog = Dialog(this)

                        val inflater = LayoutInflater.from(this)
                        val dialogView = inflater.inflate(R.layout.shop_confirm, null)
                        dialog.setContentView(dialogView)

                        val textView1 : TextView = dialogView.findViewById(R.id.textViewAnimalConfirm)
                        val textView2 : TextView = dialogView.findViewById(R.id.textViewGoldConfirm)
                        val buttonBuy : Button = dialogView.findViewById(R.id.buttonBuyAnimal)
                        val buttonCancel : Button = dialogView.findViewById(R.id.buttonCancelAnimal)
                        textView1.text = "구매하실 '" + Animal.animalNameMap2[Animal.animalNameMap[position]] +"'의 가격은"
                        textView2.text = "      " +price.toString() + "G 입니다."
                        buttonBuy.setOnClickListener {
                            val existedGold = gold - price
                            editor.putInt("gold", existedGold)
                            editor.putBoolean(Animal.animalNameMap[position],true)
                            editor.apply()
                            gold = existedGold
                            textViewGold.text = "보유: " +existedGold.toString()
                            dialog.dismiss()
                            Toast.makeText(this, "구매 완료!", Toast.LENGTH_SHORT).show()
                        }
                        buttonCancel.setOnClickListener {
                            dialog.dismiss()
                        }
                        dialog.show()


                    } else{ //골드 부족
                        val dialog = Dialog(this)
                        val inflater = LayoutInflater.from(this)
                        val dialogView = inflater.inflate(R.layout.shop_alarm, null)
                        dialog.setContentView(dialogView)
                        val btn : Button = dialogView.findViewById(R.id.buttonOK)
                        btn.setOnClickListener {
                            dialog.dismiss()
                        }
                        dialog.show()
                    }
                }
            }


        }
        val btnHome = findViewById<ImageView>(R.id.imageViewHome)
        val intentHome = Intent(this, HomeActivity::class.java)
        btnHome.setOnClickListener{
            startActivity(intentHome)
            finish()
        }
    }
}