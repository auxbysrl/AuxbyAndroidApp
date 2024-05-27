package com.fivedevs.auxby.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LabelModel(
    var language: String = "",
    var translation: String = ""
) : Parcelable