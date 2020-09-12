package com.qw.photo.functions

import com.qw.photo.callback.CoCoCallBack
import com.qw.photo.work.FunctionManager
import com.qw.photo.work.Worker

/**
 * @author cd5160866
 */
abstract class BaseFunctionBuilder<P, Result>(
    private val functionManager: FunctionManager,
    private val worker: Worker<P, Result>
) {

    init {
        this.functionManager.workerFlows.add(this)
    }

    fun then(): FunctionManager {
        return this.functionManager
    }

    /**
     * 应用参数为后续操作做准备
     */
    fun start(callback: CoCoCallBack<Result>) {
        val iterator = functionManager.workerFlows.iterator()
        if (!iterator.hasNext()) {
            return
        }
        realApply(iterator, callback)
    }

    internal abstract fun getParamsBuilder(): P

    private fun realApply(iterator: MutableIterator<Any>, callback: CoCoCallBack<Result>) {
        val worker = (iterator.next() as BaseFunctionBuilder<P, Result>).worker
        worker.start(getParamsBuilder(), object : CoCoCallBack<Result> {
            override fun onSuccess(data: Result) {
                if (iterator.hasNext()) {
                    iterator.remove()
                    realApply(iterator, callback)
                } else {
                    callback.onSuccess(data)
                }
            }

            override fun onFailed(exception: Exception) {
                callback.onFailed(exception)
            }
        })
    }

}