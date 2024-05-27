package com.fivedevs.auxby.domain.models

import com.fivedevs.auxby.domain.models.enums.LanguagesCodeEnum

data class LanguageModel(
    var codeEnum: LanguagesCodeEnum = LanguagesCodeEnum.EN,
    var isSelected: Boolean = false
)