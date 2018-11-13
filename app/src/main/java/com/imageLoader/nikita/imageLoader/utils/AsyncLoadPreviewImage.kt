package com.imageLoader.nikita.imageLoader.utils

import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import java.net.URL
import android.graphics.BitmapFactory
import java.io.IOException


class AsyncLoadPreviewImage(
    private val position: Int,
    private val set: HashSet<AsyncLoadPreviewImage>,
    private val element: ShortImageModel,
    private val adapter: ImageListViewAdapter
) :
    AsyncTask<String, Unit, Bitmap>() {
    override fun onPreExecute() {
        set.add(this)
    }

    override fun onCancelled(result: Bitmap?) {
        set.remove(this)
    }

    override fun onPostExecute(result: Bitmap?) {
        set.remove(this)
        try {
            element.previewImage = result
            adapter.notifyItemChanged(position)
        } catch (e: IndexOutOfBoundsException) {
            Log.e("image_loader_cache", e.toString())
        }

    }

    override fun doInBackground(vararg params: String?): Bitmap {
        val stringUrl = params[0]
        val imageUrl = URL(stringUrl)
        if (isCancelled) {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
        try {
            return BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream())
        } catch (e: IOException) {
            Log.e("image_loader_cache", e.toString())
        } catch (e: IllegalArgumentException) {
            Log.e("image_loader_cache", e.toString())
        }
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }

}