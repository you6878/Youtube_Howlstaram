package com.howl.howlstagram

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class AlertFragment : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view = inflater.inflate(R.layout.fragment_alarm,container,false)
        var recylcerview = view.findViewById<RecyclerView>(R.id.alarmfragment_recyclerview)
        recylcerview.adapter = AlarmRecyclerViewAdapter()
        recylcerview.layoutManager = LinearLayoutManager(activity)

        return view
    }
    inner class AlarmRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
            return CustomVieHolder(view)
        }

        inner class CustomVieHolder(view: View?) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return  3
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        }

    }
}