package fillooow.app.imhere.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fillooow.app.imhere.R
import fillooow.app.imhere.data.HelpMethodsForScheduleRecycler
import fillooow.app.imhere.db.entity.ScheduleEntity
import kotlinx.android.synthetic.main.schedule_item_view.view.*

private const val HOURS = 3
private const val MINUTES = 4

class ScheduleRecyclerViewAdapter(

    private val schedule: List<ScheduleEntity>

) : RecyclerView.Adapter<ScheduleRecyclerViewAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item_view, parent, false)
        return ScheduleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {

        holder.bind(schedule[position])
    }

    override fun getItemCount(): Int = schedule.size

    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val classNumberTV = itemView.class_number_tv
        private val classNameTV = itemView.class_name_tv
        private val classTypeTV = itemView.class_type_tv
        private val auditoryTV = itemView.auditory_tv
        private val lecturerTV = itemView.lecturer_tv
        private val pairTime = itemView.time_tv
        private val classImage = itemView.class_image

        fun bind(scheduleItem: ScheduleEntity) {

            val helpMethods = HelpMethodsForScheduleRecycler //объект с методами для помощи в заполнении ресайклера
            classNumberTV.text = scheduleItem.number.toString()
            classNameTV.text = scheduleItem.name
            classTypeTV.text = scheduleItem.type
            auditoryTV.text = scheduleItem.auditorium
            lecturerTV.text = scheduleItem.lecturer
            pairTime.text = scheduleItem.date.mapPairTime()
            classImage.setImageResource(helpMethods.getPrefixResId(helpMethods.getPrefix(scheduleItem.name)))
        }

        private fun String.mapPairTime() = "${split(',')[HOURS]}:${split(',')[MINUTES]}"
    }
}