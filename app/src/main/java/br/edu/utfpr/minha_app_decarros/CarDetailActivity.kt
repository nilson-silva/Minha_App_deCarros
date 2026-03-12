package br.edu.utfpr.minha_app_decarros

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import br.edu.utfpr.minha_app_decarros.databinding.ActivityCarDetailBinding
import br.edu.utfpr.minha_app_decarros.model.Car
import br.edu.utfpr.minha_app_decarros.model.Place
import br.edu.utfpr.minha_app_decarros.Services.CarServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CarDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCarDetailBinding
    private var car: Car? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pegando o objeto carro de forma segura
        car = intent.getSerializableExtra("CARRO_SELECIONADO") as? Car

        if (car == null) {
            setupModoCadastro()
        } else {
            setupModoVisualizacao(car!!)
        }
    }

    private fun setupModoVisualizacao(item: Car) {
        supportActionBar?.title = "Car Details"

        binding.editDetailName.setText(item.name)
        binding.editDetailModel.setText(item.model)
        binding.editDetailLicence.setText(item.licence)

        // Bloqueia campos
        binding.editDetailName.isEnabled = false
        binding.editDetailModel.isEnabled = false
        binding.editDetailLicence.isEnabled = false

        binding.labelUrl.visibility = View.GONE
        binding.editDetailImageUrl.visibility = View.GONE
        binding.btnSaveCar.visibility = View.GONE

        Picasso.get().load(item.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(binding.imgDetailPhoto)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapDetail) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupModoCadastro() {
        supportActionBar?.title = "New Car"

        binding.editDetailName.isEnabled = true
        binding.editDetailModel.isEnabled = true
        binding.editDetailLicence.isEnabled = true

        binding.labelUrl.visibility = View.VISIBLE
        binding.editDetailImageUrl.visibility = View.VISIBLE
        binding.btnSaveCar.visibility = View.VISIBLE

        // Esconde o mapa no modo cadastro para não dar erro
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapDetail)
        mapFragment?.let {
            supportFragmentManager.beginTransaction().hide(it).commit()
        }

        binding.btnSaveCar.setOnClickListener { saveCarToNodeJs() }
    }

    private fun saveCarToNodeJs() {
        val name = binding.editDetailName.text.toString()
        val licence = binding.editDetailLicence.text.toString()
        val url = binding.editDetailImageUrl.text.toString()

        if (name.isEmpty() || licence.isEmpty()) {
            Toast.makeText(this, "Preencha Nome e Placa!", Toast.LENGTH_SHORT).show()
            return
        }

        // Criamos o objeto exatamente como o README da sua API pede
        // Note que não passei 'color' nem 'model' dentro do objeto enviado
        val newCar = Car(
            id = System.currentTimeMillis().toString(),
            imageUrl = if (url.isEmpty()) "https://via.placeholder.com/300" else url,
            year = "2024",
            name = name,
            licence = licence,
            place = Place(-23.55, -46.63)
        )

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.67:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(CarServices::class.java)

        service.saveCar(newCar).enqueue(object : Callback<Car> {
            override fun onResponse(call: Call<Car>, response: Response<Car>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CarDetailActivity, "Sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // Se der 400, vamos ver a mensagem de erro do servidor no Logcat
                    Log.e("NODE_JS_ERROR", "Erro 400: ${response.errorBody()?.string()}")
                    Toast.makeText(this@CarDetailActivity, "Erro 400: Dados inválidos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Car>, t: Throwable) {
                Toast.makeText(this@CarDetailActivity, "Falha: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        car?.let { item ->
            val location = LatLng(item.place.lat, item.place.long)
            googleMap.addMarker(MarkerOptions().position(location).title(item.name))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }
}