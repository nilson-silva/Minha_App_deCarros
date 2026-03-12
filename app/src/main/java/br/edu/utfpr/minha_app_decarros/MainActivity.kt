package br.edu.utfpr.minha_app_decarros

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.utfpr.minha_app_decarros.databinding.ActivityMainBinding
import br.edu.utfpr.minha_app_decarros.model.Car
import br.edu.utfpr.minha_app_decarros.model.Place
import br.edu.utfpr.minha_app_decarros.Services.CarServices
import br.edu.utfpr.minha_app_decarros.adapter.CarAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val BASE_URL = "http://192.168.1.67:3000/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvCars.layoutManager = LinearLayoutManager(this)

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, CarDetailActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        buscarDadosDoServidor()
    }

    private fun buscarDadosDoServidor() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(CarServices::class.java)

        service.getCars().enqueue(object : Callback<List<Car>> {
            override fun onResponse(call: Call<List<Car>>, response: Response<List<Car>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!

                    binding.rvCars.adapter = CarAdapter(list)
                } else {
                    Log.e("RETROFIT_ERRO", "Erro: ${response.code()}")
                    carregarDadosLocais("Erro ${response.code()}: Usando Offline")
                }
            }

            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                Log.e("RETROFIT_FALHA", "Falha: ${t.message}")
                carregarDadosLocais("Sem conexão: Usando Offline")
            }
        })
    }
    private fun carregarDadosLocais(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()

        val localList = listOf(
            Car(
                id = "001",
                imageUrl = "https://images.noticiasautomotivas.com.br/img/f/honda-civic-2021-1.jpg",
                year = "2021",
                name = "Civic (Offline)",
                licence = "ABC-1234",
                place = Place(-23.55, -46.63), // <-- CORREÇÃO: Criando o objeto Place aqui
                model = "LXR 2.0",
                color = "Preto"
            ),
            Car(
                id = "002",
                imageUrl = "https://production.autoforce.com/uploads/picture/cache/medium_5ecfe5d2f2b3b_5ecfe5d2f2b3b.jpg",
                year = "2023",
                name = "Onix (Offline)",
                licence = "XYZ-9999",
                place = Place(-23.60, -46.70), // <-- CORREÇÃO: Criando o objeto Place aqui
                model = "Turbo",
                color = "Prata"
            )
        )
        binding.rvCars.adapter = CarAdapter(localList)
    }
}