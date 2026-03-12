package br.edu.utfpr.minha_app_decarros.model
import java.io.Serializable

data class Place(
    val lat: Double,
    val long: Double
) : Serializable

data class Car(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: Place,
    val model: String = "",
    val color: String = ""
) : Serializable