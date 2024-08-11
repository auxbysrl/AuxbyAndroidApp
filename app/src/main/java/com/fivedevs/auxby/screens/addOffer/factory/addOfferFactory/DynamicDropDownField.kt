package com.fivedevs.auxby.screens.addOffer.factory.addOfferFactory

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.widget.AppCompatTextView
import com.fivedevs.auxby.R
import com.fivedevs.auxby.domain.models.CategoryField
import com.fivedevs.auxby.domain.models.CategoryFieldsValue
import com.fivedevs.auxby.domain.utils.extensions.getName
import com.fivedevs.auxby.domain.utils.extensions.hide
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.screens.addOffer.factory.AddOfferFactoryInterface
import com.google.android.material.textfield.TextInputLayout


class DynamicDropDownField(
    private val context: Context
) : AddOfferFactoryInterface {

    private val listOfViews: MutableSet<Pair<View, CategoryField>> = mutableSetOf()

    override fun createDynamicView(categoryField: CategoryField, valueByName: String?): View {
        return View.inflate(context, R.layout.item_field_drop_down, null).apply {
            tag = categoryField.name

            listOfViews.add(Pair(this, categoryField))
            if (categoryField.parent.isNotEmpty()) this.hide()

            val borderView = findViewById<View>(R.id.border)
            val tilValue = findViewById<TextInputLayout>(R.id.tilValue)
            findViewById<AppCompatTextView>(R.id.tvTitle).apply {
                text = categoryField.label.getName(context)
            }

            val listOfOptions = categoryField.options.map { it.name.ifEmpty { it.label.getName(context) } }.sorted()

            val adapter: ArrayAdapter<String> = getArrayAdapter(listOfOptions)
            findViewById<AutoCompleteTextView>(R.id.actvValue).apply {
                hint = categoryField.placeholder.orEmpty()
                setDropDownBackgroundResource(R.color.white)

                setAdapter(adapter)
                setOnItemClickListener { _, _, position, _ ->
                    borderView.hide()
                    disableFieldError(tilValue)
                    handleChildViews(adapter.getItem(position).toString())
                }

                if (valueByName.orEmpty().isNotEmpty()) {
                   setText(valueByName, false)
                }
            }
        }
    }

    override fun validateDynamicFields(): Boolean {
        var areValid = true

        listOfViews.forEach { (view, categoryField) ->
            val borderView = view.findViewById<View>(R.id.border)
            val tilValue = view.findViewById<TextInputLayout>(R.id.tilValue)
            val actvValue = view.findViewById<AutoCompleteTextView>(R.id.actvValue)

            if (categoryField.required && actvValue.text.toString().isEmpty()) {
                enableFieldError(tilValue)
                borderView.show()
                areValid = false
            }
        }
        return areValid
    }

    override fun getDynamicFieldsValue(): List<CategoryFieldsValue> {
        val fieldsValues = mutableListOf<CategoryFieldsValue>()
        listOfViews.forEach { (view, categoryField) ->
            val actvValue = view.findViewById<AutoCompleteTextView>(R.id.actvValue)

            if (actvValue.text.toString().isNotEmpty()) {
                fieldsValues.add(CategoryFieldsValue(categoryField.name, actvValue.text.toString()))
            }
        }

        return fieldsValues
    }

    private fun enableFieldError(tilValue: TextInputLayout) {
        tilValue.boxStrokeColor = context.getColor(R.color.text_input_box_stroke_red)
        tilValue.error = context.getString(R.string.text_field_required)
        tilValue.errorIconDrawable = null
    }

    private fun AutoCompleteTextView.disableFieldError(tilValue: TextInputLayout) {
        tilValue.error = null
        tilValue.isErrorEnabled = false
        tilValue.boxStrokeColor = context.getColor(R.color.text_input_box_stroke_blue)
    }

    private fun handleChildViews(parentName: String) {
        listOfViews.filter { it.second.parent.isNotEmpty() }.firstOrNull {
            it.second.options.any { it.parentOption.equals(parentName, true) }
        }?.let {

            val listOfOptions = it.second.options.firstOrNull { fieldOption ->
                fieldOption.parentOption.equals(parentName, true)
            }?.childOptions.orEmpty()

            if (listOfOptions.isEmpty()) {
                it.first.hide()
                return
            } else {
                it.first.show()
            }

            it.first.findViewById<AutoCompleteTextView>(R.id.actvValue).apply {
                text.clear()
                setAdapter(getArrayAdapter(listOfOptions))
            }
        }
    }

    private fun getArrayAdapter(listOfOptions: List<String>) = ArrayAdapter(
        context, R.layout.item_dropdown, listOfOptions
    )
}