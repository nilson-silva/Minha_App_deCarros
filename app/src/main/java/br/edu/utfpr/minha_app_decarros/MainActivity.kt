package br.edu.utfpr.minha_app_decarros

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.utfpr.minha_app_decarros.adapter.CarAdapter
import br.edu.utfpr.minha_app_decarros.databinding.ActivityMainBinding
import br.edu.utfpr.minha_app_decarros.model.Car
import br.edu.utfpr.minha_app_decarros.model.Place
import br.edu.utfpr.minha_app_decarros.Services.CarServices
import com.google.firebase.auth.FirebaseAuth
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val BASE_URL = "http://192.168.1.67:3000/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvCars.layoutManager = LinearLayoutManager(this)

        // Botão Adicionar (FAB)
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, CarDetailActivity::class.java))
        }

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
                    binding.rvCars.adapter = CarAdapter(response.body()!!)
                } else { carregarDadosLocais() }
            }
            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                carregarDadosLocais()
            }
        })
    }

    private fun carregarDadosLocais() {
        val localList = listOf(
            Car("001", "https://images.noticiasautomotivas.com.br/img/f/honda-civic-2021-1.jpg", "2021", "Civic (Local)", "ABC-1234", Place(-25.42, -49.26)),
            Car("002", "https://production.autoforce.com/uploads/picture/cache/medium_5ecfe5d2f2b3b_5ecfe5d2f2b3b.jpg", "2023", "Onix (Local)", "XYZ-9999", Place(-25.44, -49.29))
        )
        binding.rvCars.adapter = CarAdapter(localList)
    }
}