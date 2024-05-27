package com.fivedevs.auxby.screens.addOffer.factory.addOfferFactory

import android.content.Context
import android.text.InputFilter
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import com.fivedevs.auxby.R
import com.fivedevs.auxby.domain.models.CategoryField
import com.fivedevs.auxby.domain.models.CategoryFieldsValue
import com.fivedevs.auxby.domain.utils.extensions.getName
import com.fivedevs.auxby.domain.utils.extensions.orElse
import com.fivedevs.auxby.domain.utils.extensions.orZero
import com.fivedevs.auxby.screens.addOffer.factory.AddOfferFactoryInterface
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class DynamicEditTextField(
    private val context: Context
) : AddOfferFactoryInterface {

    private val listOfViews: MutableSet<Pair<View, CategoryField>> = mutableSetOf()

    override fun createDynamicView(categoryField: CategoryField, value: String?): View {
        return View.inflate(context, R.layout.item_field_text_input, null).apply {
            tag = categoryField.name

            listOfViews.add(Pair(this, categoryField))

            findViewById<AppCompatTextView>(R.id.tvTitle).apply {
                text = categoryField.label.getName(context)
            }

            val tilValue = findViewById<TextInputLayout>(R.id.tilValue)

            findViewById<TextInputEditText>(R.id.etValue).apply {
                maxLines = 1
                isSingleLine = true
                hint = categoryField.placeholder.orEmpty()
                tag = categoryField.name
                categoryField.constraints?.let { constraints ->
                    inputType = constraints.getFieldInputType()
                    maxLines = constraints.maxLines.orElse(1)
                    filters = if (constraints.maxLength.orZero() > 0)
                        arrayOf(
                            InputFilter.LengthFilter(constraints.maxLength.orZero()),
                        ) else
                        arrayOf()
                }

                autoSetValue(this, value)

                doOnTextChanged { _, _, _, _ ->
                    tilValue.error = null
                    tilValue.isErrorEnabled = false
                }
            }
        }
    }

    private fun autoSetValue(editText: TextInputEditText, value: String?) {
        value?.let {
            editText.setText(it)
        }
    }

    override fun validateDynamicFields(): Boolean {
        var areValid = true

        listOfViews.forEach { (view, categoryField) ->
            val tilValue = view.findViewById<TextInputLayout>(R.id.tilValue)
            val etValue = view.findViewById<TextInputEditText>(R.id.etValue)
            etValue.clearFocus()
            if (categoryField.required && etValue.text.toString().isEmpty()) {
                tilValue.error = context.getString(R.string.text_field_required)
                tilValue.errorIconDrawable = null

                areValid = false
            }
        }
        return areValid
    }

    override fun getDynamicFieldsValue(): List<CategoryFieldsValue> {
        val fieldsValues = mutableListOf<CategoryFieldsValue>()
        listOfViews.forEach { (view, categoryField) ->
            val etValue = view.findViewById<TextInputEditText>(R.id.etValue)

            if (etValue.text.toString().isNotEmpty()) {
                fieldsValues.add(CategoryFieldsValue(categoryField.name, etValue.text.toString()))
            }
        }

        return fieldsValues
    }
}