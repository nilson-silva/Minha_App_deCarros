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

class CarAdapter(private val carList: List<Car>) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {
    class CarViewHolder(val binding: ItemCarBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = carList[position]
        val context = holder.itemView.context

        holder.binding.txtItemName.text = car.name
        holder.binding.txtItemLicence.text = "Licence: ${car.licence}"

        Picasso.get()
            .load(car.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.binding.imgItemPhoto)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, CarDetailActivity::class.java)
            intent.putExtra("CARRO_SELECIONADO", car)
            context.startActivity(intent)
        }
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle("Remover Carro")
                .setMessage("Deseja excluir o ${car.name} do servidor?")
                .setPositiveButton("Sim, excluir") { _, _ ->

                    val retrofit = Retrofit.Builder()
                        .baseUrl("http://192.168.1.67:3000/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val service = retrofit.create(CarServices::class.java)

                    service.deleteCar(car.id).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Carro removido!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(context, "Erro: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }
    }

    override fun getItemCount() = carList.size
}