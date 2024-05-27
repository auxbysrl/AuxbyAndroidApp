package com.fivedevs.auxby.screens.addOffer.factory.addOfferFactory

import android.content.Context
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.children
import com.fivedevs.auxby.R
import com.fivedevs.auxby.domain.models.CategoryField
import com.fivedevs.auxby.domain.models.CategoryFieldsValue
import com.fivedevs.auxby.domain.utils.extensions.getName
import com.fivedevs.auxby.screens.addOffer.factory.AddOfferFactoryInterface

class DynamicRadioBoxField(
    private val context: Context
) : AddOfferFactoryInterface {

    private val listOfViews: MutableSet<Pair<View, CategoryField>> = mutableSetOf()


    override fun createDynamicView(categoryField: CategoryField, valueByName: String?): View {
        return View.inflate(context, R.layout.item_field_radio_box, null).apply {
            tag = categoryField.name

            listOfViews.add(Pair(this, categoryField))

            findViewById<AppCompatTextView>(R.id.tvTitle).apply {
                text = categoryField.label.getName(context)
            }

            categoryField.options.forEachIndexed { index, fieldOption ->
                findViewById<RadioGroup>(R.id.rgValue).apply {
                    (getChildAt(index) as? RadioButton)?.let {
                        it.text = fieldOption.label.getName(context)

                        if (fieldOption.label.getName(context).equals(valueByName.orEmpty(), true)) {
                            it.isChecked = true
                        }
                    }

                    setOnCheckedChangeListener { _, _ ->
                        children.forEach {
                            (it as RadioButton).buttonDrawable?.setTint(context.getColor(R.color.colorPrimary))
                        }
                    }
                }
            }
        }
    }

    override fun validateDynamicFields(): Boolean {
        var areValid = true

        listOfViews.forEach { (view, categoryField) ->
            val rgValue = view.findViewById<RadioGroup>(R.id.rgValue)

            if (categoryField.required && !rgValue.children.any { (it as RadioButton).isChecked }) {
                rgValue.children.forEach {
                    (it as RadioButton).buttonDrawable?.setTint(context.getColor(R.color.red))
                }

                areValid = false
            }
        }
        return areValid
    }

    override fun getDynamicFieldsValue(): List<CategoryFieldsValue> {
        val fieldsValues = mutableListOf<CategoryFieldsValue>()
        listOfViews.forEach { (view, categoryField) ->
            val rgValue = view.findViewById<RadioGroup>(R.id.rgValue)

            if (rgValue.children.any { (it as RadioButton).isChecked }) {
                val selectedId: Int = rgValue.checkedRadioButtonId
                val checked = view.findViewById(selectedId) as RadioButton

                fieldsValues.add(CategoryFieldsValue(categoryField.name, checked.text.toString()))
            }
        }

        return fieldsValues
    }
}