package com.gify.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gify.R
import com.gify.ui.GifAdapterViewTypes.LOADING_ITEM
import com.gify.ui.GifAdapterViewTypes.GIF_MAIN_ITEM
import com.gify.ui.model.GifModel
import kotlinx.android.synthetic.main.item_grid.view.*
import kotlinx.android.synthetic.main.item_loading.view.*
import javax.inject.Inject


class GifyListAdapter @Inject constructor() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

     val gifList = ArrayList<GifModel>()
    lateinit var listener: OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            GIF_MAIN_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grid, null)
                return GifViewHolder(view)
            }
            LOADING_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading, null)
                return LoadingViewHolder(view)
            }

        }
        throw RuntimeException("not handled type")
    }

    override fun getItemCount() = gifList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GifViewHolder) {

            Glide.with(holder.gifImage.context)
                .asGif()
                .load(gifList[position].previewGifUrl)
                .placeholder(R.drawable.undraw_loading_frh4)
                .error(R.drawable.undraw_page_not_found_su7k)
                .into(holder.gifImage)
            holder.itemView.setOnClickListener {
                listener.onClick(position, it)
            }
        } else if (holder is LoadingViewHolder) {

        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (gifList[position].type == LOADING_ITEM)
            LOADING_ITEM
        else
            GIF_MAIN_ITEM
    }

    //main item
    class GifViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val gifImage: ImageView = view.gifImage
    }

    //loading item
    class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val progressBar: ProgressBar = view.progressBar
    }

    fun addGifs(list: ArrayList<GifModel>) {
        gifList.addAll(list)
        notifyDataSetChanged()
    }

    fun clearAllGIF(){
        gifList.clear()
        notifyDataSetChanged()
    }

    fun addLoadingData() {
        gifList.add(
            GifModel(
                "fake_id",
                "fake_title",
                "fake_url",
                "fake_url",
                1
            )
        );
        notifyItemInserted(gifList.size - 1);
    }

    fun removeLoadingData() {
        gifList.remove(gifList[gifList.size - 1])
        notifyItemRemoved(gifList.size - 1)
    }

    fun setClickListener(listener: OnClickListener) {
        this.listener = listener
    }

}


