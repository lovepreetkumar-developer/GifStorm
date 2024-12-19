package com.techfathers.gifstorm.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.techfathers.gifstorm.R
import com.techfathers.gifstorm.models.SimpleStringModel

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class AutoCompleteAdapter(
    context: Context,
    private val searchResults: List<SimpleStringModel>
) : ArrayAdapter<SimpleStringModel>(context, R.layout.item_search_location, searchResults),
    Filterable {

    private var mSearchResultModel: List<SimpleStringModel> = searchResults

    override fun getCount(): Int {
        return mSearchResultModel.size
    }

    override fun getItem(p0: Int): SimpleStringModel {
        return mSearchResultModel[p0]

    }

    override fun getItemId(p0: Int): Long {
        // Or just return p0
        return mSearchResultModel[p0].id.toLong()
    }

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_search_location, null)

        val textView = view.findViewById<TextView>(R.id.tv_content)
        textView.text = mSearchResultModel[position].name

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: FilterResults
            ) {
                mSearchResultModel = filterResults.values as List<SimpleStringModel>
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()?.lowercase()
                val filterResults = FilterResults()
                filterResults.values = if (queryString == null || queryString.isEmpty())
                    searchResults
                else
                    searchResults.filter {
                        it.name.lowercase().contains(queryString)
                    }

                return filterResults
            }
        }
    }
}