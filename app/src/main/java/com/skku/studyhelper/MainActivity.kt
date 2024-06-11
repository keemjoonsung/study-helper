package com.skku.studyhelper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.KakaoJson
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val KAKAO_NATIVE_KEY = BuildConfig.KAKAO_NATIVE_KEY
        val NAVER_CLIENT_ID = BuildConfig.NAVER_CLIENT_ID
        val NAVER_CLIENT_SECRET = BuildConfig.NAVER_CLIENT_SCRETE
        val keyHash = Utility.getKeyHash(this)
        val kakaoBtn = findViewById<ImageView>(R.id.fake_kakao)

        val naverBtn = findViewById<ImageView>(R.id.fake_naver)

        //val intent = Intent(this,HomeActivity::class.java )
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
                    // 로그인 성공 시 HomeActivity로 이동
                    UserApiClient.instance.me { user, meError ->
                        if (meError != null) {
                            Log.e("KakaoLogin", "사용자 정보 요청 실패", meError)
                        } else if (user != null) {
                            val nickname = user.kakaoAccount?.profile?.nickname
                            Log.i(
                                "KakaoLogin",
                                "사용자 정보 요청 성공: ${nickname}, ID: ${user.id}"
                            )

                            // 사용자 이름과 고유 ID를 Intent에 추가
                            val intent = Intent(this, HomeActivity::class.java).apply {
                                putExtra("USER_NAME", nickname)
                                putExtra("USER_ID", "KAKAO_${user.id.toString()}")
                            }
                            startActivity(intent)
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
                    val userId = result.profile?.id
                    val userName = result.profile?.name
                    Log.i("NaverLogin", "사용자 정보 요청 성공: ${userName}, ID: ${userId}")

                    // 사용자 이름과 고유 ID를 Intent에 추가
                    val intent = Intent(this@MainActivity, HomeActivity::class.java).apply {
                        putExtra("USER_NAME", userName)
                        putExtra("USER_ID", "NAVER_${userId}") // 고유 ID에 'NAVER_' 접두사 추가
                    }
                    startActivity(intent)
                }


            }
            val oauthLoginCallback = object : OAuthLoginCallback {
                override fun onSuccess() {
                    NidOAuthLogin().callProfileApi(nidProfileCallback)
                }


                override fun onFailure(httpStatus: Int, message: String) {
                    val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                    val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                    Log.e("test", "$errorCode $errorDescription")
                }
                override fun onError(errorCode: Int, message: String) {
                    onFailure(errorCode, message)
                }
            }

            NaverIdLoginSDK.authenticate(this, oauthLoginCallback)

        }

    }
}