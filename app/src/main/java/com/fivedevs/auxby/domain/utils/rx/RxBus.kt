package com.fivedevs.auxby.domain.utils.rx

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class RxBus {

    private val bus = PublishSubject.create<Any>().toSerialized()

    fun send(o: Any) {
        bus.onNext(o)
    }

    fun error(o: String) {
        bus.onError(Exception(o))
    }

    fun toObservable(): Observable<Any> {
        return bus
    }
}