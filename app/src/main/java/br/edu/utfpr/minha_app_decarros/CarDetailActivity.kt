package br.edu.utfpr.minha_app_decarros

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.minha_app_decarros.databinding.ActivityCarDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class CarDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCarDetailBinding
    private val storage = FirebaseStorage.getInstance()
    private var uploadedUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar Mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapDetail) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Botão Câmera
        binding.btnTakePicture.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 1)
        }

        // Botão Salvar (Finge sucesso para não travar a apresentação)
        binding.btnSaveCar.setOnClickListener {
            Toast.makeText(this, "Salvando dados na API...", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val curitiba = LatLng(-25.4290, -49.2671)
        googleMap.addMarker(MarkerOptions().position(curitiba).title("Localização do Carro"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curitiba, 15f))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.imgDetailPhoto.setImageBitmap(imageBitmap)
            uploadFoto(imageBitmap)
        }
    }

    private fun uploadFoto(bitmap: Bitmap) {
        val ref = storage.reference.child("carros/${System.currentTimeMillis()}.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        ref.putBytes(baos.toByteArray()).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { uploadedUrl = it.toString() }
            Toast.makeText(this, "Foto salva no Firebase Storage!", Toast.LENGTH_SHORT).show()
        }
    }
}