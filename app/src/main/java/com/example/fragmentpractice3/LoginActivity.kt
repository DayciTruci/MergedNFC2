package com.example.fragmentpractice3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fragmentpractice3.databinding.ActivityLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.singUp.setOnClickListener {
            startActivity(Intent(this, SingInAcvivity::class.java))
        }

        //회원가입 액티비티에서 보낸 이메일과 비밀번호를 받는다.
        var email = intent.getStringExtra("email") ?: return
        var password = intent.getStringExtra("password") ?: return

        // 로그인 번튼 클릭 시
        binding.loginPageLoginButton.setOnClickListener {
            Firebase.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {  task ->
                    if(task.isSuccessful){
                        //로그인 성공시
                        Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                        
                    }else{
                        Log.e("로그인액티비티",task.exception.toString())
                        Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }





        loginPageMainTextView.setOnClickListener {
            Toast.makeText(this, "눌림", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }
}