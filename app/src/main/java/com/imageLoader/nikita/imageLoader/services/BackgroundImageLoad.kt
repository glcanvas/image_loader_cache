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
            Log.e("image_loader_cache", "url is null")
            return
        }
        val regex = Regex("[^a-zA-Z0-9]")
        val correctFile = regex.replace(url, "")
        if (fileExist(correctFile)) {
            loadFileFromStorage(correctFile, broadcastIntent)
        } else {
            loadFileFromInternet(imageUrl, correctFile, broadcastIntent)
        }


    }

    private fun loadFileFromInternet(imageUrl: URL, correctFile: String, broadcastIntent: Intent) {
        try {
            val inputStream = imageUrl.openConnection().getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val resultBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, false)
            broadcastIntent.putExtra(DetailFragment.PARAM_STATUS, "ok")
            broadcastIntent.putExtra(DetailFragment.PARAM_RESULT, resultBitmap)
            sendBroadcast(broadcastIntent)
            saveFileToStorage(correctFile, resultBitmap)
            Log.d("image_loader_cache", "${File(baseContext.cacheDir, correctFile)} load from internet")
        } catch (e: IOException) {
            broadcastIntent.putExtra(DetailFragment.PARAM_STATUS, "fail")
            sendBroadcast(broadcastIntent)
            Log.e("image_loader_cache", e.toString())
        } catch (e: IllegalArgumentException) {
            broadcastIntent.putExtra(DetailFragment.PARAM_STATUS, "fail")
            sendBroadcast(broadcastIntent)
            Log.e("image_loader_cache", e.toString())
        }
    }

    private fun loadFileFromStorage(way: String, broadcastIntent: Intent) {
        try {
            val cache = baseContext.cacheDir
            val correctFile = File(cache, way).toString()
            val bitmap = BitmapFactory.decodeStream(FileInputStream(correctFile))
            broadcastIntent.putExtra(DetailFragment.PARAM_STATUS, "ok")
            broadcastIntent.putExtra(DetailFragment.PARAM_RESULT, bitmap)
            sendBroadcast(broadcastIntent)
            Log.d("image_loader_cache", "file $correctFile load")
        } catch (e: IOException) {
            broadcastIntent.putExtra(DetailFragment.PARAM_STATUS, "fail")
            sendBroadcast(broadcastIntent)
            Log.e("image_loader_cache", e.toString())
        }
    }

    private fun saveFileToStorage(way: String, bitmap: Bitmap) {
        val cache = baseContext.cacheDir
        val correctFile = File(cache, way).path
        val fos = FileOutputStream(correctFile)
        fos.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        Log.d("image_loader_cache", "file $correctFile save")
    }

    private fun fileExist(way: String): Boolean {
        val cache = baseContext.cacheDir
        val file = File(cache, way)
        val result = file.exists()
        Log.d("image_loader_cache", "file ${file.toString()} exist=$result")
        return result
    }
}