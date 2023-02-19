package com.ghadeer.retrofitrequestqueue

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity: AppCompatActivity() {

    companion object{
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val apiInterface = ApiClient().getClient()

        val call1 = apiInterface.getProduct("1")
        val callback1 = object: Callback<Product>{
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                Log.d(TAG, "onResponse: product 1 ${response.body()?.toString()}")
            }
            override fun onFailure(call: Call<Product>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.localizedMessage}")
            }
        }

        val call2 = apiInterface.getProduct("2")
        val callback2 = object: Callback<Product>{
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                Log.d(TAG, "onResponse: product 2 ${response.body()?.toString()}")
            }
            override fun onFailure(call: Call<Product>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.localizedMessage}")
            }
        }

        val call3 = apiInterface.getProduct("3")
        val callback3 = object: Callback<Product>{
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                Log.d(TAG, "onResponse: product 3 ${response.body()?.toString()}")
            }
            override fun onFailure(call: Call<Product>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.localizedMessage}")
            }
        }

        val call4 = apiInterface.getProduct("4")
        val callback4 = object: Callback<Product>{
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                Log.d(TAG, "onResponse: product 4 ${response.body()?.toString()}")
            }
            override fun onFailure(call: Call<Product>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.localizedMessage}")
            }
        }

        RetrofitRequestsQueue.retrofitQueueCallback = object :
            RetrofitRequestsQueue.RetrofitQueueCallback(){
            override fun onQueueStart() {
                // On Queue start
                Log.d(TAG, "Queue Started")
            }

            override fun onQueueFinish() {
                // On Queue finish
                Log.d(TAG, "Queue Finished")
            }
            override fun onQueuePause() {
                // On Queue pause
                Log.d(TAG, "Queue Paused")
            }
        }
        RetrofitRequestsQueue.autoRun = false
        RetrofitRequestsQueue.addRequest("Product 3 request", call3, callback3)
        RetrofitRequestsQueue.addRequest("Product 1 request", call1, callback1)
        RetrofitRequestsQueue.addRequest("Product 4 request", call4, callback4)
        RetrofitRequestsQueue.addRequest("Product 2 request", call2, callback2)
        Handler().postDelayed({RetrofitRequestsQueue.run()}, 2000)
        Handler().postDelayed({RetrofitRequestsQueue.pause()}, 5000)
        Handler().postDelayed({RetrofitRequestsQueue.run()}, 10000)
    }
}