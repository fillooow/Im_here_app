package fillooow.app.imhere.utils

import android.widget.EditText
import fillooow.app.imhere.extensions.isEnteredDateLessThanCurrent
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.redmadrobot.inputmask.model.Notation
import fillooow.app.imhere.extensions.textAsString

private const val DATE_PATTERN = "dd.MM.yyyy"

// https://github.com/RedMadRobot/input-mask-android/wiki/1.-Mask-Syntax
private const val DATE_MASK = "[d][0]{.}[m][0]{.20}[00]"

class DateMaskedTextChangedListener {

    fun installListener(editText: EditText, errorCallback: () -> Unit) {

        MaskedTextChangedListener.Companion.installOn(

            primaryFormat = DATE_MASK,
            autocomplete = true,
            autoskip = true,
            editText = editText,
            customNotations = listOf(dayNotation, monthNotation),
            valueListener = object : MaskedTextChangedListener.ValueListener {

                override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String) {

                    if (maskFilled && editText.textAsString.isDateValid()) {
                        if (editText.isEnteredDateLessThanCurrent()) {
                            errorCallback.invoke()
                        }
                    }
                }
            }
        )
    }

    /**
     * [dayNotation] - нужна, так как первое число даты не может быть больше 3
     * [monthNotation] - нужна, так как первое число месяца не может быть больше 1
     *
     * https://github.com/RedMadRobot/input-mask-android/wiki/1.1-Custom-Notations
     */
    private val dayNotation = Notation('d', "0123", true)
    private val monthNotation = Notation('m', "01", true)

    private fun String.isDateValid() = matches(Regex("""\d{1,2}.\d{1,2}.20\d{2}"""))
}