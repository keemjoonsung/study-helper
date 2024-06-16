package com.skku.studyhelper

import android.app.Application

class Animal {
    companion object {
        val animalMap: Map<String, Int> = mapOf(
            "frog" to R.drawable.frog,
            "koala" to R.drawable.koala,
            "panda" to R.drawable.panda
        )
        val animalPriceMap : Map<Int, Int> = mapOf(
            0 to 0,
            1 to 1000,
            2 to 5000
        )
        val animalGoldMap : Map<String, Int> = mapOf(
            "frog" to 10,
            "koala" to 20,
            "panda" to 30
        )
        val animalNameMap: Map<Int, String> = mapOf(
            0 to "frog",
            1 to "koala",
            2 to "panda"
        )
        val animalNameMap2: Map<String, String> = mapOf(
            "frog" to "개구리",
            "koala" to "코알라",
            "panda" to "판다"
        )
        val animalExplainMap : Map<Int, String> = mapOf(
            0 to "이름 : 개구리 \n\n\n1분당 골드 획득량 : 10G\n\n\n가격 : 무료",
            1 to "이름 : 코알라 \n\n\n1분당 골드 획득량 : 20G\n\n\n가격 : 1000G",
            2 to "이름 : 판다 \n\n\n1분당 골드 획득량 : 30G\n\n\n가격 : 5000G"

        )

    }


}