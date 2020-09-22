package com.example.tinkoff

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.tinkoff.retrofit.Network
import com.example.tinkoff.retrofit.Post
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var imageGif: ImageView
    private lateinit var description: TextView
    private lateinit var tableLayout: TabLayout
    private var myList: MutableList<Post> = ArrayList()
    private var latestList:MutableList<Post> = ArrayList()
    private var topList:MutableList<Post> = ArrayList()

    private var index: Int = 0 //Для рандома
    private var latIndex: Int = 0 //Последние
    private var topIndex:Int = 0 //Топ
    private var position:Int?=0
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnRepeat: Button
    private lateinit var btnLatest: Button

    companion object {
        private const val SAVED_INDEX = "index"
        private const val SAVED_LATINDEX = "latindex"
        private const val SAVED_TOPINDEX = "topindex"
        private const val SAVED_POSTS = "list"
        private const val SAVED_LATESTPOSTS = "latestlist"
        private const val SAVED_TOPPOSTS = "toplist"
        private const val SAVED_TABSTATE = "tabState"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        description = findViewById(R.id.desc)
        imageGif = findViewById(R.id.imgView)
        btnPrev = findViewById(R.id.btn_prev)
        btnNext = findViewById(R.id.btn_next)
        btnRepeat = findViewById(R.id.btn_repeat)
        tableLayout = findViewById(R.id.tabLayout)

        if (savedInstanceState != null) {
            myList = savedInstanceState.getParcelableArrayList<Post>(SAVED_POSTS) as MutableList<Post>
            latestList = savedInstanceState.getParcelableArrayList<Post>(SAVED_LATESTPOSTS) as MutableList<Post>
            topList = savedInstanceState.getParcelableArrayList<Post>(SAVED_TOPPOSTS) as MutableList<Post>
            index = savedInstanceState.getInt(SAVED_INDEX)
            latIndex=savedInstanceState.getInt(SAVED_LATINDEX)
            topIndex=savedInstanceState.getInt(SAVED_TOPINDEX)
            position=savedInstanceState.getInt(SAVED_TABSTATE)
            displaySubject(myList[index].gifUrl!!, myList[index].desc.toString())
            if (myList.size != 0) btnPrev.isEnabled = true
        }
        else {
            loadPost()
        }

        tableLayout.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
            position = tab?.position
                when (position)
                {
                    0 -> {
                        if (index < myList.size) {
                            displaySubject(myList[index].gifUrl!!.replace("http","https"), myList[index].desc.toString())
                        } else {
                            loadPost()
                        }
                    }

                    1-> {
                       // latIndex++
                        if (latIndex < latestList.size ) {
                            displaySubject(latestList[latIndex].gifUrl!!.replace("http","https"), latestList[latIndex].desc.toString())
                        } else {
                          loadLatestPost()
                        }
                    }

                    2-> {
                       // topIndex++
                        if (topIndex < topList.size ) {
                            displaySubject(topList[topIndex].gifUrl!!.replace("http","https"), topList[topIndex].desc.toString())
                        } else {
                          loadTopPost()
                        }
                    }
                    else -> {
                       // loadPost()
                    }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        buttonPrevBlock()
        btnRepeat.setOnClickListener {
            loadPost()
        }

        btnNext.setOnClickListener {
            btnRepeat.visibility = View.GONE

            when (position){
                0->{
                   index++

                    if (index < myList.size) {
                        displaySubject(myList[index].gifUrl!!.replace("http","https"), myList[index].desc.toString())
                    } else {
                        loadPost()

                    }
                }
                1->{
                   latIndex++
                    if (latIndex < latestList.size) {
                        displaySubject(latestList[latIndex].gifUrl!!.replace("http","https"), latestList[latIndex].desc.toString())
                    } else {
                     loadLatestPost()
                    }
                }
                2->{
                   topIndex++
                    if (topIndex < topList.size) {
                        displaySubject(topList[topIndex].gifUrl!!.replace("http","https"), topList[topIndex].desc.toString())
                    } else {
                      loadTopPost()
                    }
                }
            }
            buttonPrevBlock()
        }

        btnPrev.setOnClickListener {
            btnRepeat.visibility = View.GONE
            when (position)
            {
                0->{  index--
                    displaySubject(myList[index].gifUrl?.replace("http","https")!!, myList[index].desc.toString())
                    buttonPrevBlock()}
                1->{
                    latIndex--
                    displaySubject(latestList[latIndex].gifUrl?.replace("http","https")!!, latestList[latIndex].desc.toString())
                    buttonPrevBlock()
                }
                2->{
                    topIndex--
                    displaySubject(topList[topIndex].gifUrl?.replace("http","https")!!, topList[topIndex].desc.toString())
                    buttonPrevBlock()
                }

            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(SAVED_POSTS, ArrayList(myList))
        outState.putParcelableArrayList(SAVED_LATESTPOSTS, ArrayList(latestList))
        outState.putParcelableArrayList(SAVED_TOPPOSTS, ArrayList(topList))
        outState.putInt(SAVED_INDEX, index)
        outState.putInt(SAVED_LATINDEX, latIndex)
        outState.putInt(SAVED_TOPINDEX, topIndex)
        outState.putInt(SAVED_TABSTATE,position!!)
        super.onSaveInstanceState(outState)
    }

    private fun loadPost() {
        btnRepeat.visibility = View.GONE
        btnNext.visibility = View.GONE
        btnPrev.visibility = View.GONE
        GlobalScope.launch(Dispatchers.Main)
        {
            try {
                val response = Network().api.getGifs()
                if (response.isSuccessful) {
                    btnRepeat.visibility = View.GONE
                    btnNext.visibility = View.VISIBLE
                    btnPrev.visibility = View.VISIBLE
                    displaySubject(
                        response.body()?.gifUrl!!.replace("http", "https"), response.body()?.desc!!
                    )
                    myList.add(response.body()!!)
                }

            } catch (e: Exception) {
                // Toast.makeText(this@MainActivity, "Seems like error", Toast.LENGTH_SHORT).show()
                btnNext.visibility = View.GONE
                btnPrev.visibility = View.GONE
                imageGif.setImageResource(R.drawable.errorconnect)
                description.setText(R.string.error)
                btnRepeat.visibility = View.VISIBLE
            }
        }

    }

    private fun loadLatestPost()
    {
        CoroutineScope(Dispatchers.Main).launch()
        {
            try {
                val response = Network().api.getLatest(0)

                if (response.isSuccessful) {
                    btnRepeat.visibility = View.GONE
                    btnNext.visibility = View.VISIBLE
                    btnPrev.visibility = View.VISIBLE
                    displaySubject(response.body()!!.result[latIndex].gifUrl!!.replace("http","https"),
                        response.body()!!.result[latIndex].desc.toString())
                    latestList.add(response.body()!!.result[latIndex])
                }
            } catch (e: Exception) {
                // Toast.makeText(this@MainActivity, "Seems like error", Toast.LENGTH_SHORT).show()
                btnNext.visibility = View.GONE
                btnPrev.visibility = View.GONE
                imageGif.setImageResource(R.drawable.errorconnect)
                description.setText(R.string.error)
                btnRepeat.visibility = View.VISIBLE
                // latIndex = 0
            }

        }
    }
    private fun loadTopPost()
    {
        CoroutineScope(Dispatchers.Main).launch()
        {
            try {
                val response = Network().api.getTops(0)

                if (response.isSuccessful) {
                    btnRepeat.visibility = View.GONE
                    btnNext.visibility = View.VISIBLE
                    btnPrev.visibility = View.VISIBLE
                    response.body()?.result!![topIndex].gifUrl?.let { it1 ->
                        response.body()!!.result[topIndex].desc?.let { it2 ->
                            displaySubject(
                                it1.replace("http", "https"),
                                it2
                            )
                        }
                    }
                    topList.add(response.body()!!.result[topIndex])
                }
            } catch (e: Exception) {
                // Toast.makeText(this@MainActivity, "Seems like error", Toast.LENGTH_SHORT).show()
                btnNext.visibility = View.GONE
                btnPrev.visibility = View.GONE
                imageGif.setImageResource(R.drawable.errorconnect)
                description.setText(R.string.error)
                btnRepeat.visibility = View.VISIBLE
                // topIndex = 0
            }
        }
    }

    private fun buttonPrevBlock() {
        btnPrev.isEnabled = index != 0
    }

    private fun displaySubject(url: String, desc: String) {
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.loadingicon)
            .error(R.drawable.errorconnect)
            .into(imageGif)
        description.text = desc
    }

}




