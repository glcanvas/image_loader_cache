package com.imageLoader.nikita.imageLoader


import android.os.Bundle
import android.support.v4.app.Fragment

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.imageLoader.nikita.imageLoader.utils.AsyncLoadList
import com.imageLoader.nikita.imageLoader.utils.AsyncLoadPreviewImage
import com.imageLoader.nikita.imageLoader.utils.ImageListViewAdapter
import com.imageLoader.nikita.imageLoader.utils.ShortImageModel
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class ImageListFragment : Fragment() {
    private val metaImages = ArrayList<ShortImageModel>()

    private val taskSet = HashSet<AsyncLoadList>()
    private val previewImageSet = HashSet<AsyncLoadPreviewImage>()

    private var screenSize: String? = null
    private var offset: Int = 1
    private lateinit var adapter: ImageListViewAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var button: Button
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bundle = this.arguments ?: Bundle()
        val view = inflater.inflate(R.layout.image_list_view, container, false)

        screenSize = bundle.getString("screen_size") ?: screenSize
        recyclerView = view.findViewById(R.id.image_list)
        adapter = ImageListViewAdapter(metaImages)
        recyclerView.adapter = adapter

        offset = bundle.getInt("offset") ?: 1
        bundle.putAll(savedInstanceState ?: Bundle())
        val tmpArray =
            bundle.getParcelableArrayList("list_images") ?: listOf<ShortImageModel>()
        metaImages.addAll(tmpArray)
        adapter.notifyDataSetChanged()
        metaImages.forEachIndexed { index, item ->
            if (item.previewImage == null) {
                AsyncLoadPreviewImage(index, previewImageSet, item, adapter).execute(item.previewLink)
            }
        }

        button = view.findViewById(R.id.switch_frame)
        button.setOnClickListener {
            val async = AsyncLoadList(offset, adapter, metaImages, taskSet, previewImageSet)
            async.execute("https://api.unsplash.com/photos/?client_id=73e14423b06e6a0f7715e4ea90b0c9b8f3e94fa21d6281ed2b730da4cb79d016")
            offset++
        }

        recyclerView.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                bundle.putInt("list_offset", offset)
                bundle.putString("full_url", metaImages[position].fullLink)
                val currentFragment = DetailFragment()
                currentFragment.arguments = bundle
                if (screenSize == "normal") {
                    fragmentManager?.beginTransaction()?.replace(R.id.listholder, currentFragment)?.addToBackStack(null)
                        ?.commit()
                } else {
                    fragmentManager?.beginTransaction()?.replace(R.id.detailholder, currentFragment)?.commit()
                }
            }
        })
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        for (i in taskSet) {
            i.cancel(true)
        }
        for (i in previewImageSet) {
            i.cancel(true)
        }
        taskSet.clear()
        previewImageSet.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val tmp = metaImages.map { x ->
            ShortImageModel(
                authorName = x.authorName,
                description = x.description,
                previewLink = x.previewLink,
                fullLink = x.fullLink,
                previewImage = x.previewImage,
                fullImage = null
            )
        } as ArrayList<ShortImageModel>
        outState.putParcelableArrayList("list_images", tmp)
        outState.putInt("offset", offset)
    }


    interface OnItemClickListener {
        fun onItemClicked(position: Int, view: View)
    }

    fun RecyclerView.addOnItemClickListener(onClickListener: OnItemClickListener) {
        this.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewDetachedFromWindow(view: View) {
                view.setOnClickListener(null)
            }

            override fun onChildViewAttachedToWindow(view: View) {
                view.setOnClickListener {
                    val holder = getChildViewHolder(view)
                    onClickListener.onItemClicked(holder.adapterPosition, view)
                }
            }
        })

    }
}
