package br.edu.utfpr.minha_app_decarros

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.minha_app_decarros.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Se o usuário já estiver logado, pula direto para a tela principal
        if (auth.currentUser != null) {
            irParaPrincipal()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.editEmail.text.toString()
            val senha = binding.editPassword.text.toString()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, senha)
                    .addOnSuccessListener {
                        irParaPrincipal()
                    }
                    .addOnFailureListener {
                        // Agora o Toast vai funcionar!
                        Toast.makeText(this, "Erro: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun irParaPrincipal() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Fecha a tela de login
    }
}