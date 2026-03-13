package br.edu.utfpr.minha_app_decarros

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import br.edu.utfpr.minha_app_decarros.databinding.ActivityCarDetailBinding
import br.edu.utfpr.minha_app_decarros.model.Car
import br.edu.utfpr.minha_app_decarros.model.Place
import br.edu.utfpr.minha_app_decarros.Services.CarServices
import com.google.android.gms. maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class CarDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCarDetailBinding
    private var car: Car? = null
    private var imageBitmap: Bitmap? = null

    private lateinit var mMap: GoogleMap
    private var currentMarker: Marker? = null

    // Coordenada padrão (caso o usuário não escolha, começa em uma posição neutra)
    private var selectedLatLng: LatLng = LatLng(-23.5505, -46.6333)

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data?.extras?.get("data") as Bitmap
            binding.imgDetailPhoto.setImageBitmap(data)
            imageBitmap = data
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        car = intent.getSerializableExtra("CARRO_SELECIONADO") as? Car

        // Inicializa o mapa (SupportMapFragment)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapDetail) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (car == null) {
            setupModoCadastro()
        } else {
            setupModoVisualizacao(car!!)
        }
    }

    private fun setupModoVisualizacao(item: Car) {
        supportActionBar?.title = "Car Details"
        binding.editDetailName.setText(item.name)
        binding.editDetailLicence.setText(item.licence)
        binding.editDetailName.isEnabled = false
        binding.editDetailLicence.isEnabled = false

        binding.labelUrl.visibility = View.GONE
        binding.editDetailImageUrl.visibility = View.GONE
        binding.btnSaveCar.visibility = View.GONE
        binding.btnTakePicture.visibility = View.GONE

        Picasso.get().load(item.imageUrl).placeholder(android.R.drawable.ic_menu_gallery).into(binding.imgDetailPhoto)
    }

    private fun setupModoCadastro() {
        supportActionBar?.title = "New Car Location"
        binding.labelUrl.visibility = View.VISIBLE
        binding.editDetailImageUrl.visibility = View.VISIBLE
        binding.btnSaveCar.visibility = View.VISIBLE
        binding.btnTakePicture.visibility = View.VISIBLE

        binding.btnTakePicture.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureLauncher.launch(intent)
        }

        binding.btnSaveCar.setOnClickListener {
            if (imageBitmap != null) {
                uploadImageToFirebase()
            } else {
                saveCarToNodeJs(binding.editDetailImageUrl.text.toString())
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (car != null) {
            // MODO VISUALIZAÇÃO: Mapa travado na posição do carro
            val carLocation = LatLng(car!!.place.lat, car!!.place.long)
            mMap.addMarker(MarkerOptions().position(carLocation).title(car!!.name))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(carLocation, 15f))
        } else {
            // MODO CADASTRO: Mapa interativo
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 12f))

            // 1. OnMapClickListener: Clique para definir o local
            mMap.setOnMapClickListener { latLng ->
                atualizarMarcador(latLng)
            }

            // 2. Configura o marcador inicial como arrastável (Drag & Drop)
            atualizarMarcador(selectedLatLng)

            // 3. OnMarkerDragListener: Captura a posição final após o arraste
            mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(p0: Marker) {}
                override fun onMarkerDrag(p0: Marker) {}
                override fun onMarkerDragEnd(marker: Marker) {
                    selectedLatLng = marker.position // Sincroniza a coordenada
                    Toast.makeText(this@CarDetailActivity, "Position updated!", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun atualizarMarcador(latLng: LatLng) {
        currentMarker?.remove() // Remove o marcador anterior
        selectedLatLng = latLng // Salva a nova coordenada

        currentMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Car Location")
                .draggable(true) // Habilita o Drag & Drop
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun saveCarToNodeJs(finalUrl: String) {
        val name = binding.editDetailName.text.toString()
        val licence = binding.editDetailLicence.text.toString()

        if (name.isEmpty() || licence.isEmpty()) {
            Toast.makeText(this, "Complete Name and Licence!", Toast.LENGTH_SHORT).show()
            return
        }

        // SINCRONIZAÇÃO: Enviando as coordenadas capturadas do Mapa (selectedLatLng)
        val newCar = Car(
            id = System.currentTimeMillis().toString(),
            imageUrl = if (finalUrl.isEmpty()) "https://via.placeholder.com/300" else finalUrl,
            year = "2024",
            name = name,
            licence = licence,
            place = Place(lat = selectedLatLng.latitude, long = selectedLatLng.longitude)
        )

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.67:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(CarServices::class.java)

        service.saveCar(newCar).enqueue(object : Callback<Car> {
            override fun onResponse(call: Call<Car>, response: Response<Car>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CarDetailActivity, "Car Saved with Location!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            override fun onFailure(call: Call<Car>, t: Throwable) {
                Toast.makeText(this@CarDetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // Função de upload para o Firebase (conforme fizemos ontem)
    private fun uploadImageToFirebase() {
        val storageRef = FirebaseStorage.getInstance().reference.child("cars/${System.currentTimeMillis()}.jpg")
        val baos = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        storageRef.putBytes(data).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                saveCarToNodeJs(uri.toString())
            }
        }
    }
}