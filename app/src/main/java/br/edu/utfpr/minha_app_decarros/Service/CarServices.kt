package br.edu.utfpr.minha_app_decarros.Services

import br.edu.utfpr.minha_app_decarros.model.Car
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CarServices {
    @GET("car")
    fun getCars(): Call<List<Car>>

    @POST ("car")
    fun saveCar(@Body car: Car): Call<Car>

    @DELETE("car/{id}")
    fun deleteCar(@Path("id") id: String): Call<Void>


}