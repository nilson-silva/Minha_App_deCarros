package br.edu.utfpr.minha_app_decarros.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.minha_app_decarros.CarDetailActivity
import br.edu.utfpr.minha_app_decarros.Services.CarServices
import br.edu.utfpr.minha_app_decarros.databinding.ItemCarBinding
import br.edu.utfpr.minha_app_decarros.model.Car
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CarAdapter(private var carList: List<Car>) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {
    class CarViewHolder(val binding: ItemCarBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding)
    }
    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = carList[position]
        val context = holder.itemView.context

        holder.binding.txtItemName.text = car.name
        holder.binding.txtItemLicence.text = "Placa: ${car.licence}"

        Picasso.get().load(car.imageUrl).into(holder.binding.imgItemPhoto)

        holder.binding.btnShareItem.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val message = "Olha esse carro que cadastrei no meu app: ${car.name}\nPlaca: ${car.licence}\nLink da foto: ${car.imageUrl}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, message)
            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Carro via:"))
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, CarDetailActivity::class.java)
            intent.putExtra("CARRO_SELECIONADO", car)
            context.startActivity(intent)
        }

        holder.binding.btnDeleteItem.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Excluir")
                .setMessage("Deseja apagar o ${car.name}?")
                .setPositiveButton("Sim") { _, _ -> deletarCarroDoServidor(car.id, context) }
                .setNegativeButton("Não", null)
                .show()
        }
    }

    override fun getItemCount(): Int = carList.size
    fun updateList(newList: List<Car>) {
        this.carList = newList
        notifyDataSetChanged()
    }

    private fun deletarCarroDoServidor(id: String, context: android.content.Context) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.67:3000/") // Seu IP Local
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(CarServices::class.java)

        service.deleteCar(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Carro removido com sucesso!", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(context, "Erro ao deletar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Falha de conexão com o PC", Toast.LENGTH_SHORT).show()
            }
        })
    }
}