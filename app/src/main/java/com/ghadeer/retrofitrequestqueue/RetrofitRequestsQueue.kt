package com.ghadeer.retrofitrequestqueue

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


/**
 * @author Ghadeer Hammoud
 * @since 23-07-2022
 * This class represents a retrofit requests queue to execute retrofit calls in order.
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

    private var status: Status = Status.STOPPED
    var retrofitQueueCallback: RetrofitQueueCallback? = null
    var autoRun: Boolean = false

    /**
     * Add a request to the queue.
     * @param tag a string identifier of the request.
     * @param call retrofit call object.
     * @param callback retrofit callback object of the request.
     */
    fun addRequest(tag: String, call: Call<*>, callback: Callback<*>){
        requestsQueue.add(QueueItem(tag, call as Call<Any>, callback as Callback<Any>, requestsQueue.size + 1))
        Log.d(TAG, "addRequest: request '$tag' added to the queue")
        if(autoRun) run()
    }

    /**
     * Add a request to the front of the queue.
     * @param tag a string identifier of the request.
     * @param call retrofit call object.
     * @param callback retrofit callback object of the request.
     */
    fun addRequestToFront(tag: String, call: Call<*>, callback: Callback<*>){
        val minPriority = requestsQueue.minByOrNull { it.priority }?.priority ?: 1
        requestsQueue.add(QueueItem(tag, call as Call<Any>, callback as Callback<Any>, minPriority - 1))
        if(autoRun) run()
    }

    /**
     * Add a collection of requests to the queue.
     * @param tags string identifiers of the requests.
     * @param calls retrofit calls objects list.
     * @param callbacks retrofit callbacks objects list.
     * @throws IllegalArgumentException when number of calls is not matching the number of callbacks and tags.
     */
    fun addRequests(tags: List<String>, calls: List<Call<*>>, callbacks: List<Callback<*>>){
        if(calls.size != callbacks.size || calls.size != tags.size){
            throw IllegalArgumentException("Number of calls must match the number of callbacks and tags.")
        }

        for(i in calls.indices)
            requestsQueue.add(QueueItem(tags[i], calls[i] as Call<Any>, callbacks[i] as Callback<Any>, requestsQueue.size + 1))

        if(autoRun) run()
    }

    fun removeRequest(tag: String){
        requestsQueue.find { it.tag == tag }?.let {
            requestsQueue.remove(it)
        }
    }

    fun removeRequests(tags: List<String>){
        requestsQueue.filter { it.tag in tags }.forEach {
            requestsQueue.remove(it)
        }
    }

    fun clearQueue(){
        requestsQueue.clear()
    }

    fun run(){
        if(status != Status.RUNNING){
            Log.d(TAG, "Retrofit Queue: started.")
            status = Status.RUNNING
            retrofitQueueCallback?.onQueueStart()
            tryToExecuteNextRequest()
        }
    }

    fun pause(){
        status = Status.PAUSED
        retrofitQueueCallback?.onQueuePause()
    }

    fun stop(){
        requestsQueue.clear()
        status = Status.STOPPED
        retrofitQueueCallback?.onQueueFinish()
    }

    private fun tryToExecuteNextRequest(){

        if(status != Status.RUNNING)
            return

        if (requestsQueue.isNotEmpty()){

            requestsQueue.remove().apply {
                Log.d(TAG, "Retrofit Queue: start sending request $tag [priority = $priority]")
                call.enqueue(object : Callback<Any> {
                    override fun onResponse(
                        call: Call<Any>,
                        response: Response<Any>
                    ) {
                        Log.d(TAG, "Retrofit Queue: response of request $tag has been received successfully [priority = $priority]")
                        callback.onResponse(call, response)
                        tryToExecuteNextRequest()
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        Log.d(TAG, "onFailure: $tag (priority = $priority)")
                        Log.d(TAG, "Retrofit Queue: Failure in executing request $tag [priority = $priority]")
                        callback.onFailure(call, t)
                        tryToExecuteNextRequest()
                    }

                })
            }
        }
        else{
            Log.d(TAG, "Retrofit Queue: Requests Queue is empty.")
            Log.d(TAG, "Retrofit Queue: stop running.")
            status = Status.STOPPED
            retrofitQueueCallback?.onQueueFinish()
        }

    }

    abstract class RetrofitQueueCallback{
        abstract fun onQueueStart()
        abstract fun onQueueFinish()
        abstract fun onQueuePause()
    }
}



class QueueItem<T>(
    var tag: String,
    var call: Call<T>,
    var callback: Callback<T>,
    var priority: Int
)


class RequestsComparator {
    companion object : Comparator<QueueItem<Any>> {
        override fun compare(a: QueueItem<Any>, b: QueueItem<Any>): Int = when{
            a.priority != b.priority -> a.priority - b.priority
            else -> 0
        }
    }
}

enum class Status{
    RUNNING, PAUSED, STOPPED
}