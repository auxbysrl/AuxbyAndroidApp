package com.fivedevs.auxby.domain.utils.rx

import io.reactivex.rxjava3.core.Scheduler

interface RxSchedulers {

    fun io(): Scheduler

    fun single(): Scheduler

    fun network(): Scheduler

    fun androidUI(): Scheduler

    fun trampoline(): Scheduler

    fun background(): Scheduler

    fun computation(): Scheduler
}
