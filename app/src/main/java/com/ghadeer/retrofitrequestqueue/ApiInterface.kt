package com.ghadeer.retrofitrequestqueue

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {
    @GET("products/{id}")
    fun getProduct(@Path("id") id: String): Call<Product>
}