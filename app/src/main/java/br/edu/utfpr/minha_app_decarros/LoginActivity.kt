package br.edu.utfpr.minha_app_decarros

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.minha_app_decarros.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSendCode.setOnClickListener {
            val phone = binding.editTextPhone.text.toString()
            if (phone.isNotEmpty()) {
                binding.editTextCode.visibility = View.VISIBLE
                binding.btnVerifyCode.visibility = View.VISIBLE
                binding.btnSendCode.visibility = View.GONE
                Toast.makeText(this, "Código enviado para $phone", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnVerifyCode.setOnClickListener {
            val code = binding.editTextCode.text.toString()
            if (code == "123456") {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Código incorreto! Use 123456", Toast.LENGTH_SHORT).show()
            }
        }
    }
}