package com.example.fragmentpractice3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.fragmentpractice3.databinding.ActivitySingInAcvivityBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SingInAcvivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingInAcvivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySingInAcvivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.SingUpPageButton.setOnClickListener {

            // 아이디와 비밀번호를 String형태로 들고온다.
            val email = binding.SingUpPageIdEditText.text.toString()
            val password = binding.SingUpPagePasswordEditText.text.toString()

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "이메일 또는 패스워드가 입력되지 않았습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener (this){ task ->
                    if(task.isSuccessful){
                        //회원가입 성공
                        Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this,LoginActivity::class.java)
                        intent.putExtra("email",email)
                        intent.putExtra("password",password)

                        startActivity(intent)
                    }else{
                        // 회원가입 실패
                        Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}