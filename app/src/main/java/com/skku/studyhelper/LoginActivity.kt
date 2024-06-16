package com.skku.studyhelper

import android.app.AlertDialog
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import java.util.Calendar

class LoginActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharedPreferences: SharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val intent = Intent(this,HomeActivity::class.java )

        if(sharedPreferences.getBoolean("isLogin",false)){ //자동 로그인
            startActivity(intent)
            finish()
        }
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
        }

        val KAKAO_NATIVE_KEY = BuildConfig.KAKAO_NATIVE_KEY
        val NAVER_CLIENT_ID = BuildConfig.NAVER_CLIENT_ID
        val NAVER_CLIENT_SECRET = BuildConfig.NAVER_CLIENT_SCRETE
        val keyHash = Utility.getKeyHash(this)
        val kakaoBtn = findViewById<ImageView>(R.id.fake_kakao)

        val naverBtn = findViewById<ImageView>(R.id.fake_naver)

        KakaoSdk.init(this, KAKAO_NATIVE_KEY)
        NaverIdLoginSDK.initialize(this, NAVER_CLIENT_ID,NAVER_CLIENT_SECRET , "Study Helper")
        kakaoBtn.setOnClickListener{
            if(UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
                    //카카오톡앱이 설치되어 있을 때 -> 앱 실행
            }else{
            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                if (error != null) {
                    Log.e("KakaoLogin", "로그인 실패", error)
                } else if (token != null) {
                    Log.i("KakaoLogin", "로그인 성공. Access Token: ${token.accessToken}")
                    UserApiClient.instance.me { user, meError ->
                        if (meError != null) {
                            Log.e("KakaoLogin", "사용자 정보 요청 실패", meError)
                        } else if (user != null) {
                            val nickname = user.kakaoAccount?.profile?.nickname

                            editor.putBoolean("isLogin",true)
                            editor.putString("id", nickname)
                            editor.putString("timer", "00:00")
                            editor.putInt("gold",1000)
                            editor.putString("profile","frog")
                            editor.putBoolean("frog",true)
                            editor.putBoolean("panda",false)
                            editor.putBoolean("koala",false)

                            editor.apply()
                            startActivity(intent)
                            finish()
                        }
                    }


                }
            }
            }

        }

        naverBtn.setOnClickListener {

            val nidProfileCallback  = object : NidProfileCallback<NidProfileResponse> {
                override fun onFailure(httpStatus: Int, message: String) {
                    Log.e("NaverLogin", "사용자 정보 요청 실패: $message")
                }

                override fun onError(errorCode: Int, message: String) {
                    onFailure(errorCode, message)
                }

                override fun onSuccess(result: NidProfileResponse) {
                    val userName = result.profile?.name

                    editor.putBoolean("isLogin",true)
                    editor.putString("id", userName)
                    editor.putString("timer", "00:00")
                    editor.putInt("gold",1000)
                    editor.putString("profile","frog")
                    editor.putBoolean("frog",true)
                    editor.putBoolean("panda",false)
                    editor.putBoolean("koala",false)
                    editor.apply()
                    startActivity(intent)
                    finish()
                }

            }
            val oauthLoginCallback = object : OAuthLoginCallback {
                override fun onSuccess() {
                    NidOAuthLogin().callProfileApi(nidProfileCallback)
                }


                override fun onFailure(httpStatus: Int, message: String) {
                    val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                    val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                    Log.e("err", "$errorCode $errorDescription")
                }
                override fun onError(errorCode: Int, message: String) {
                    onFailure(errorCode, message)
                }
            }

            NaverIdLoginSDK.authenticate(this, oauthLoginCallback)

        }

    }

    private fun hasUsageStatsPermission(): Boolean {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTime = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            currentTime - 1000 * 60 * 60 * 24, currentTime
        )
        return stats != null && stats.isNotEmpty()
    }

    private fun requestUsageStatsPermission() {
        AlertDialog.Builder(this).apply {
            setTitle("권한 필요")
            setMessage("이 앱은 사용량 접근 권한이 필요합니다. 설정에서 권한을 허용해 주세요.")
            setPositiveButton("설정으로 이동") { _, _ ->
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            setNegativeButton("취소", null)
            show()
        }
    }


}