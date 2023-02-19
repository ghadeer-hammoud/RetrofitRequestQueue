package com.ghadeer.retrofitrequestqueue

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.log

/**
 * This class was created on 23-07-2022 by Ghadeer Hammoud
 * This class represents a retrofit requests queue to execute retrofit calls in a sequence.
 * This class is used when we need a request1 to be "executed" and "responded" before request2
 */
object RetrofitRequestsQueue {

    const val TAG = "RetrofitRequestsQueue"

    /**
     * Requests PriorityQueue
     * The request having the less number of priority will be executed first.
     * If R1 has priority 1 and R2 has priority 2 -> R1 will be executed first.
     */
    private var requestsQueue: PriorityQueue<QueueItem<Any>> = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        PriorityQueue<QueueItem<Any>>(RequestsComparator)
    } else
        PriorityQueue<QueueItem<Any>>()

    var isWorking: Boolean = false
    var retrofitQueueCallback: RetrofitQueueCallback? = null

    fun addRequest(tag: String, call: Call<Any>, callback: Callback<Any>){
        requestsQueue.add(QueueItem(tag, call, callback, requestsQueue.size + 1, false))
        Log.d(TAG, "addRequest: request added to the queue")
        run()
    }

    fun addRequestToFront(tag: String, call: Call<Any>, callback: Callback<Any>){
        val minPriority = requestsQueue.minWithOrNull(Comparator.comparingInt {it.priority })?.priority ?: 1
        requestsQueue.add(QueueItem(tag, call, callback, minPriority - 1, false))
        run()
    }

    fun addRequests(tag: String, calls: List<Call<Any>>, callbacks: List<Callback<Any>>){
        if(calls.size != callbacks.size){
            Log.e(TAG, "addRequests: Number of calls must match the number of callbacks")
            return
        }

        for(i in 0 .. calls.size)
            requestsQueue.add(QueueItem(tag, calls[i], callbacks[i], requestsQueue.size + 1, false))

        run()
    }

    fun clearQueue(){
        requestsQueue.clear()
    }

    private fun run(){
        if(!isWorking){
            Log.d(TAG, "run: start working")
            isWorking = true
            retrofitQueueCallback?.onQueueStart()
            tryToExecuteNextRequest()
        }
    }

    private fun tryToExecuteNextRequest(){

        if (requestsQueue.isNotEmpty()){

            requestsQueue.remove().apply {
                Log.d(TAG, "tryToExecuteNextRequest: send a new request $tag")
                call.enqueue(object : Callback<Any> {
                    override fun onResponse(
                        call: Call<Any>,
                        response: Response<Any>
                    ) {
                        Log.d(TAG, "onResponse: $tag (priority = $priority)")
                        callback.onResponse(call, response)
                        tryToExecuteNextRequest()
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        Log.d(TAG, "onFailure: $tag (priority = $priority)")
                        callback.onFailure(call, t)
                        tryToExecuteNextRequest()
                    }
                })
            }
        }
        else{
            Log.d(TAG, "tryToExecuteNextRequest: Requests Queue IS EMPTY")
            Log.d(TAG, "tryToExecuteNextRequest: stop working")
            isWorking = false
            retrofitQueueCallback?.onQueueFinish()
        }

    }

    abstract class RetrofitQueueCallback{
        abstract fun onQueueStart()
        abstract fun onQueueFinish()
    }
}



class QueueItem<T>(
    var tag: String,
    var call: Call<T>,
    var callback: Callback<T>,
    var priority: Int,
    var isDone: Boolean
)


class RequestsComparator {
    companion object : Comparator<QueueItem<Any>> {
        override fun compare(a: QueueItem<Any>, b: QueueItem<Any>): Int = when{
            a.priority != b.priority -> a.priority - b.priority
            else -> 0
        }
    }
}