package com.imageLoader.nikita.imageLoader

import android.content.Context
import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager


class MainActivity : AppCompatActivity() {

    lateinit var fragmentManager: FragmentManager
    lateinit var screenSize: String

    fun getDimension(context: Context): String {
        val screenLayout = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        return when (screenLayout) {
            Configuration.SCREENLAYOUT_SIZE_SMALL -> "normal"
            Configuration.SCREENLAYOUT_SIZE_NORMAL -> "normal"
            Configuration.SCREENLAYOUT_SIZE_LARGE -> "large"
            Configuration.SCREENLAYOUT_SIZE_XLARGE -> "large"
            else -> "undefined"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        fragmentManager = supportFragmentManager
        screenSize = getDimension(baseContext)
        val bundle = Bundle()
        bundle.putString("screen_size", screenSize)
        when (screenSize) {
            "normal" -> {
                if (fragmentManager.fragments.size == 0) {


                    val fragment = savedInstanceState?.getString("active_fragment")
                    val currentFragment: Fragment
                    when (fragment) {
                        "detail" -> currentFragment = DetailFragment()
                        else -> currentFragment = ImageListFragment()
                    }
                    bundle.putAll(savedInstanceState ?: Bundle())
                    currentFragment.arguments = bundle
                    fragmentManager.beginTransaction().replace(R.id.listholder, currentFragment).commit()
                }
            }
            "large" -> {
                if (fragmentManager.fragments.size == 0) {
                    val listFragment = ImageListFragment()
                    val detailFragment = DetailFragment()
                    bundle.putAll(savedInstanceState ?: Bundle())
                    listFragment.arguments = bundle
                    detailFragment.arguments = bundle
                    fragmentManager.beginTransaction().replace(R.id.listholder, listFragment).commit()
                    fragmentManager.beginTransaction().replace(R.id.detailholder, detailFragment).commit()
                }
            }
            else -> {
                throw Error()
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString("screen_size", screenSize)
        if (screenSize == "normal") {
            if (supportFragmentManager.findFragmentById(R.id.listholder) is ImageListFragment) {
                outState?.putString("active_fragment", "list")
            } else {
                outState?.putString("active_fragment", "detail")
            }
        }

    }
}
