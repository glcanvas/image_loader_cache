package com.imageLoader.nikita.imageLoader.services

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.imageLoader.nikita.imageLoader.DetailFragment
import java.io.*
import java.net.URL

class BackgroundImageLoad : IntentService("LoadImageService") {
    override fun onHandleIntent(intent: Intent?) {
        val url = intent?.getStringExtra("full_back_url")
        val imageUrl = URL(url)
        val broadcastIntent = Intent()
        broadcastIntent.action = DetailFragment.PROCESS_RESPONSE
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT)
        if (url == null) {
            broadcastIntent.putExtra(DetailFragment.PARAM_STATUS, "fail")
            sendBroadcast(broadcastIntent)
            Log.e("image:loader", "url is null")
            return
        }
        val regex = Regex("[^a-zA-Z0-9]")
        val correctWay = regex.replace(url, "")
        if (fileExist(correctWay)) {
            loadFileFromStorage(correctWay, broadcastIntent)
        } else {
            loadFileFromInternet(imageUrl,correctWay, broadcastIntent)
        }


    }

    private fun loadFileFromInternet(imageUrl: URL, correctWay:String, broadcastIntent: Intent) {
        try {
            val inputStream = imageUrl.openConnection().getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val resultBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, false)
            broadcastIntent.putExtra(DetailFragment.PARAM_STATUS, "ok")
            broadcastIntent.putExtra(DetailFragment.PARAM_RESULT, resultBitmap)
            sendBroadcast(broadcastIntent)
            saveFileToStorage(correctWay, resultBitmap)
            Log.d("image_loader", "$correctWay load from internet")
        } catch (e: IOException) {
            broadcastIntent.putExtra(DetailFragment.PARAM_STATUS, "fail")
            sendBroadcast(broadcastIntent)
            Log.e("image_loader", e.toString())
        } catch (e: IllegalArgumentException) {
            broadcastIntent.putExtra(DetailFragment.PARAM_STATUS, "fail")
            sendBroadcast(broadcastIntent)
            Log.e("image_loader", e.toString())
        }
    }

    private fun loadFileFromStorage(way: String, broadcastIntent: Intent) {
        try {

            val bitmap = BitmapFactory.decodeStream(openFileInput(way))
            broadcastIntent.putExtra(DetailFragment.PARAM_STATUS, "ok")
            broadcastIntent.putExtra(DetailFragment.PARAM_RESULT, bitmap)
            sendBroadcast(broadcastIntent)
            Log.d("image_loader", "file $way load")
        } catch (e: IOException) {
            broadcastIntent.putExtra(DetailFragment.PARAM_STATUS, "fail")
            sendBroadcast(broadcastIntent)
            Log.e("image_loader", e.toString())
        }
    }

    private fun saveFileToStorage(way: String, bitmap: Bitmap) {
        val fos = openFileOutput(way, Context.MODE_PRIVATE)
        fos.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        Log.d("image_loader", "file $way save")
    }

    private fun fileExist(way: String): Boolean {
        val file = baseContext.getFileStreamPath(way)
        return file.exists()
    }
}