package br.edu.utfpr.minha_app_decarros.Services

import br.edu.utfpr.minha_app_decarros.model.Car
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CarServices {
    @GET("items")
    fun getCars(): Call<List<Car>>

    @POST ("items")
    fun saveCar(@Body car: Car): Call<Car>

    @DELETE("items/{id}")
    fun deleteCar(@Path("id") id: String): Call<Void>


}