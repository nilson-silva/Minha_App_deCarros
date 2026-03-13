package br.edu.utfpr.minha_app_decarros

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.utfpr.minha_app_decarros.adapter.CarAdapter
import br.edu.utfpr.minha_app_decarros.databinding.ActivityMainBinding
import br.edu.utfpr.minha_app_decarros.model.Car
import br.edu.utfpr.minha_app_decarros.Services.CarServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var fullList: List<Car> = listOf() // Cópia da lista para o filtro de busca

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvCars.layoutManager = LinearLayoutManager(this)

        binding.swipeRefresh.setOnRefreshListener {
            buscarDadosDoServidor()
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, CarDetailActivity::class.java))
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarLista(newText)
                return true
            }
        })
        binding.swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    override fun onResume() {
        super.onResume()
        buscarDadosDoServidor()
    }

    private fun buscarDadosDoServidor() {
        binding.swipeRefresh.isRefreshing = true

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.67:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(CarServices::class.java)

        service.getCars().enqueue(object : Callback<List<Car>> {
            override fun onResponse(call: Call<List<Car>>, response: Response<List<Car>>) {
                binding.swipeRefresh.isRefreshing = false
                if (response.isSuccessful && response.body() != null) {
                    fullList = response.body()!! // Salva a lista original
                    binding.rvCars.adapter = CarAdapter(fullList)
                }
            }

            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                binding.swipeRefresh.isRefreshing = false
                Log.e("RETROFIT", t.message.toString())
            }
        })
    }

    private fun filtrarLista(texto: String?) {
        val listaFiltrada = fullList.filter { car ->
            car.name.contains(texto ?: "", ignoreCase = true)
        }
        (binding.rvCars.adapter as? CarAdapter)?.updateList(listaFiltrada)
    }
}