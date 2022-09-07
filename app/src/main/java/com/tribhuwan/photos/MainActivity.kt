package com.tribhuwan.photos

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.ImagePickerMode
import com.esafirm.imagepicker.features.ImagePickerSavePath
import com.esafirm.imagepicker.features.registerImagePicker
import com.esafirm.imagepicker.model.Image
import com.tribhuwan.photos.adapter.PhotosAdapter
import com.tribhuwan.photos.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val images = arrayListOf<Image>()

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var adapter: PhotosAdapter
    private var isLoading = false
    private var sequenceSize = 50
    private var nextLimit = 10

    var rowsArrayList = arrayListOf<String?>()


    @SuppressLint("NotifyDataSetChanged")
    private val imagePickerLauncher = registerImagePicker {
        images.clear()
        images.addAll(it)
        adapter.updatePhotos(images)
        addRows()
        if (images.isNotEmpty()) {
            sequenceSize = activityMainBinding.sequenceSize.text.ifEmpty { 50 }.toString().toInt()
            adapter.updateIndexList(
                rowsArrayList
            )
            adapter.notifyDataSetChanged()
        }
    }

    private fun addRows() {
        rowsArrayList.clear()
        if (nextLimit > sequenceSize) {
            nextLimit = sequenceSize
        }
        for (i in 1..nextLimit) {
            rowsArrayList.add(i.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        adapter = PhotosAdapter()
        adapter.updateIndexList(rowsArrayList)
        adapter.updatePhotos(images)
        activityMainBinding.rvPhotos.adapter = adapter
        activityMainBinding.rvPhotos.layoutManager = LinearLayoutManager(this)
        countListener()
        clickListeners()
        initScrollListener()
    }

    private fun initScrollListener() {
        activityMainBinding.rvPhotos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == rowsArrayList.size - 1 && rowsArrayList.size < sequenceSize) {
                        loadMoreItems()
                        isLoading = true
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMoreItems() {
        rowsArrayList.add(null)
        adapter.notifyItemInserted(rowsArrayList.size - 1)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            rowsArrayList.removeAt(rowsArrayList.size - 1)
            val scrollPosition = rowsArrayList.size
            adapter.notifyItemRemoved(scrollPosition)
            nextLimit = scrollPosition + 10
            if (nextLimit > sequenceSize) {
                nextLimit = sequenceSize
            }

            for (i in scrollPosition + 1..nextLimit) {
                rowsArrayList.add(i.toString())
            }
            adapter.notifyDataSetChanged()
            isLoading = false

        }, 1500)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clickListeners() {
        activityMainBinding.selectImages.setOnClickListener {
            imagePickerLauncher.launch(createConfig())
        }
        activityMainBinding.updateCount.setOnClickListener {
            if (activityMainBinding.sequenceSize.text.toString().toInt() > 0) {
                sequenceSize =
                    activityMainBinding.sequenceSize.text.ifEmpty { 50 }.toString().toInt()
                rowsArrayList.clear()
                addRows()
                adapter.notifyDataSetChanged()
                Toast.makeText(this, getString(R.string.sequence_updated), Toast.LENGTH_SHORT)
                    .show()
                activityMainBinding.rvPhotos.scrollToPosition(0)
            } else {
                Toast.makeText(this, getString(R.string.size_message), Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun countListener() {
        activityMainBinding.sequenceSize.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    activityMainBinding.updateCount.isVisible =
                        s.isNotEmpty() && adapter.itemCount > 0
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun createConfig(): ImagePickerConfig {
        return ImagePickerConfig {
            mode = ImagePickerMode.MULTIPLE
            showDoneButtonAlways = true
            limit = 2
            isShowCamera = false
            savePath =
                ImagePickerSavePath("Gallery")
            savePath = ImagePickerSavePath(
                Environment.getExternalStorageDirectory().path,
                isRelative = false
            )
            selectedImages = images
        }
    }
}