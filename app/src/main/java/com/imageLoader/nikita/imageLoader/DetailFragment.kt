package com.imageLoader.nikita.imageLoader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import com.imageLoader.nikita.imageLoader.services.BackgroundImageLoad

class DetailFragment : Fragment() {
    companion object {
        val PROCESS_RESPONSE = "com.image_loader.nikita.image_loader.PROCESS_RESPONSE"
        val PARAM_STATUS = "status"
        val PARAM_RESULT = "result"
    }

    private lateinit var broadcastReceiver: BroadcastReceiver
    lateinit var imageHolder: ImageView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.image_detail_view, container, false)
        imageHolder = view.findViewById(R.id.detailsImage)
        progressBar = view.findViewById(R.id.full_image_load_bar)
        val bundle = this.arguments

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val status = intent?.getStringExtra(PARAM_STATUS)
                if (status == "ok") {
                    val image = intent.getParcelableExtra<Bitmap>(PARAM_RESULT)
                    progressBar.visibility = View.GONE
                    imageHolder.setImageBitmap(image)
                } else {
                    progressBar.visibility = View.GONE
                }
            }
        }

        val filter = IntentFilter(PROCESS_RESPONSE)
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        context?.registerReceiver(broadcastReceiver, filter)

        val url = bundle?.getString("full_url")
        if (url != null) {

            val intent = Intent(this.context, BackgroundImageLoad::class.java)
            intent.putExtra("full_back_url", url)
            context?.startService(intent)
        }
        return view
    }
    override fun onDestroy() {
        context?.unregisterReceiver(broadcastReceiver)
        super.onDestroy()

    }

}