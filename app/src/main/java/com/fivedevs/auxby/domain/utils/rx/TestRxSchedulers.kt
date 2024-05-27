package com.fivedevs.auxby.domain.utils.rx

import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers

class TestRxSchedulers : RxSchedulers {

    override fun io(): Scheduler {
        return Schedulers.trampoline()
    }

    override fun single(): Scheduler {
        return Schedulers.single()
    }

    override fun network(): Scheduler {
        return Schedulers.trampoline()
    }

    override fun androidUI(): Scheduler {
        return Schedulers.trampoline()
    }

    override fun background(): Scheduler {
        return Schedulers.trampoline()
    }

    override fun trampoline(): Scheduler {
        return Schedulers.trampoline()
    }

    override fun computation(): Scheduler {
        return Schedulers.trampoline()
    }
}
