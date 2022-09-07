package com.tribhuwan.photos.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.model.Image
import com.tribhuwan.photos.databinding.ItemLoadingBinding
import com.tribhuwan.photos.databinding.ItemPhotoBinding
import kotlin.math.sqrt


class PhotosAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    private var photos = listOf<Image>()
    var rowsArrayList = listOf<String?>()


    fun updatePhotos(photos: List<Image>) {
        this.photos = photos
    }

    fun updateIndexList(rowsArrayList: ArrayList<String?>) {
        this.rowsArrayList = rowsArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            PhotoViewHolder(
                ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            LoadingViewHolder(
                ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (rowsArrayList[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PhotoViewHolder) {
            if (photos.isNotEmpty()) {
                val index = rowsArrayList[position]
                holder.binding.index.text = index
                if (isTriangularNumber(num = index?.toLong()!!)) {
                    Glide.with(holder.itemView).load(photos[0].uri)
                        .into(holder.binding.sequenceImage)
                    return
                }
                Glide.with(holder.itemView).load(photos[1].uri).into(holder.binding.sequenceImage)
            }
        }
    }

    override fun getItemCount(): Int = rowsArrayList.size


    inner class LoadingViewHolder(val binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class PhotoViewHolder(val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    private fun isTriangularNumber(num: Long): Boolean {
        val calcNum = 8 * num + 1
        val sqRoot = sqrt(calcNum.toDouble()).toLong()
        return sqRoot * sqRoot == calcNum
    }
}