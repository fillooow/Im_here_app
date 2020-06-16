package fillooow.app.imhere

import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
private const val DATE_PATTERN = "dd.MM.yyyy"

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {

        val pattern = DATE_PATTERN
        val dateFormat = SimpleDateFormat(pattern)
        val currentDate = dateFormat.parse(dateFormat.format(Date()))!!
        val etDate = dateFormat.parse("20.01.2023")!!

//        println(etDate)
        val kal = Calendar.getInstance()
        kal.set(2020, 1, 30)
//        println(kal.get(Calendar.DAY_OF_MONTH))




        val month = "20.02.2023".split(".")[1].toInt()
        val metDate = dateFormat.parse("20.02.2023")!!

        println(month)
        println(metDate.month + 1)
        println(month == metDate.month + 1)
    }
}
