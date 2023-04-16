/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zj.android.performance.jank

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zj.android.performance.R

class MessageListAdapter(
    private val messageList: Array<String>
) : RecyclerView.Adapter<MessageListAdapter.MessageHeaderViewHolder>() {

    class MessageHeaderViewHolder(
        private val view: View
    ) : RecyclerView.ViewHolder(view) {

        fun bind(headerText: String) {
            itemView.setOnClickListener {
            }
            itemView.findViewById<TextView>(R.id.messageHeader).text = headerText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHeaderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MessageHeaderViewHolder(
            inflater.inflate(R.layout.message_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MessageHeaderViewHolder, position: Int) {
        holder.bind(messageList[position])
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}
