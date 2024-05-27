package com.fivedevs.auxby.domain.utils

import androidx.lifecycle.Observer

// Used to handle this exception: Cannot add the same observer with different lifecycles
class EmptyObserver : Observer<Boolean?> {
    override fun onChanged(it: Boolean?) {
        // Do nothing
    }
}