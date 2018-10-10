import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.btp.me.classroom.Class.ChatMessage
import com.btp.me.classroom.Class.MessageType
import com.btp.me.classroom.R
import kotlinx.android.synthetic.main.my_command_layout.view.*
import kotlinx.android.synthetic.main.my_first_message_layout.view.*
import kotlinx.android.synthetic.main.my_message_layout.view.*
import kotlinx.android.synthetic.main.other_first_message_layout.view.*
import kotlinx.android.synthetic.main.other_message_layout.view.*

class ChatAdapter(private val list: ArrayList<ChatMessage>, private val context: Context) : BaseAdapter() {



    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val chatMessage = list.get(position)
        val view:View = convertView ?: when(chatMessage.viewType){
            MessageType.MY_MESSAGE-> LayoutInflater.from(context).inflate(R.layout.my_message_layout,null,false)
            MessageType.MY_FIRST_MESSAGE-> LayoutInflater.from(context).inflate(R.layout.my_first_message_layout,null,false)
            MessageType.OTHER_MESSAGE-> LayoutInflater.from(context).inflate(R.layout.other_message_layout,null,false)
            MessageType.OTHER_FIRST_MESSAGE-> LayoutInflater.from(context).inflate(R.layout.other_first_message_layout,null,false)
            else-> LayoutInflater.from(context).inflate(R.layout.my_command_layout,null,false)
        }
        chatMessage.viewType = MessageType.MY_COMMAND
        MyViewHolder(view).bind(chatMessage)

        return view
    }

    override fun getItem(position: Int): Any {
        return list.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getViewTypeCount(): Int {
        return 5
    }



    class MyViewHolder(val view:View):RecyclerView.ViewHolder(view){
//        fun setCommand(command:String,type:Int){
//            when(type){
//                MessageType.MY_COMMAND-> view.my_command_view.text = command
//                else->return
//            }
//        }

        fun bind(chatMessage: ChatMessage){
            setName(chatMessage.senderName, chatMessage.viewType)
            setMessage(chatMessage.message, chatMessage.viewType)
            setTime(chatMessage.time, chatMessage.viewType)
        }

        private fun setName(name:String,type:Int){
            when(type){
                MessageType.OTHER_FIRST_MESSAGE->view.other_first_name_view.text = name
                else-> return
            }
        }
        private fun setMessage(message:String,type: Int){
            when(type){
                MessageType.MY_COMMAND->view.my_command_view.text = message
                MessageType.MY_FIRST_MESSAGE->view.my_first_message_view.text = message
                MessageType.MY_MESSAGE->view.my_message_view.text = message
                MessageType.OTHER_FIRST_MESSAGE->view.other_first_message_view.text = message
                MessageType.OTHER_MESSAGE->view.other_message_view.text = message
                else-> return
            }
        }
        private fun setTime(time:String, type: Int){
            when(type){
                MessageType.MY_FIRST_MESSAGE->view.my_first_time_view.text = time
                MessageType.MY_MESSAGE->view.my_time_view.text = time
                MessageType.OTHER_FIRST_MESSAGE->view.other_first_time_view.text = time
                MessageType.OTHER_MESSAGE->view.other_time_view.text = time
                else-> return
            }
        }
    }

}