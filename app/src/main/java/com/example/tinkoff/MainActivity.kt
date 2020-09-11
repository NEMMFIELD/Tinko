package com.example.tinkoff

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.tinkoff.retrofit.Network
import com.example.tinkoff.retrofit.Post
import com.example.tinkoff.retrofit.Result
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    private lateinit var imageGif: ImageView
    private lateinit var description: TextView

    private var myList: MutableList<Post> = ArrayList()
    private var index: Int = 0
    private var supIndex:Int=-1
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnRepeat: Button
    private lateinit var btnLatest: Button

    companion object {
        private const val SAVED_INDEX = "index"
        private const val SAVED_POSTS = "list"
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

        buttonPrevBlock()

        if (savedInstanceState != null) {
            myList =
                savedInstanceState.getParcelableArrayList<Post>(SAVED_POSTS) as MutableList<Post>
            index = savedInstanceState.getInt(SAVED_INDEX)
            displaySubject(myList[index].gifUrl.toString(),myList[index].desc.toString())
            if (myList.size != 0) btnPrev.isEnabled = true
        } else {
            loadPost()
        }

        btnRepeat.setOnClickListener {
            loadPost()
        }

        btnNext.setOnClickListener {
            btnRepeat.visibility = View.GONE
            index++
            if (index < myList.size) {
                displaySubject(myList[index].gifUrl.toString(),myList[index].desc.toString())
            } else {
                loadPost()
            }
            buttonPrevBlock()
        }


        btnPrev.setOnClickListener {
            btnRepeat.visibility = View.GONE
            index--
            displaySubject(myList[index].gifUrl.toString(),myList[index].desc.toString())
            buttonPrevBlock()
        }
        btnLatest = findViewById(R.id.btn_latest)

        //Если нажали на кнопку Latest
        btnLatest.setOnClickListener {
supIndex++
            CoroutineScope(Dispatchers.Main).launch()
            {
                try {
                    val response = Network().api.getLatest(0)

                    if (response.isSuccessful) {
                        btnRepeat.visibility = View.GONE
                        btnNext.visibility = View.VISIBLE
                        btnPrev.visibility = View.VISIBLE
                        response.body()?.result?.get(supIndex)?.gifUrl?.let { it1 -> response.body()!!.result.get(supIndex).desc?.let { it2 ->
                            displaySubject(it1.replace("http","https"),
                                it2
                            )
                        } }
                        //myList.add(response.body()!!)
                    }


                } catch (e: Exception) {
                    // Toast.makeText(this@MainActivity, "Seems like error", Toast.LENGTH_SHORT).show()
                    btnNext.visibility = View.GONE
                    btnPrev.visibility = View.GONE
                    imageGif.setImageResource(R.drawable.errorconnect)
                    description.setText(R.string.error)
                    btnRepeat.visibility = View.VISIBLE
                    supIndex=0
                }

            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(SAVED_POSTS, ArrayList(myList))
        outState.putInt(SAVED_INDEX, index)
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
                    displaySubject(response.body()?.gifUrl!!.replace("http","https"),response.body()?.desc!!)
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



        /*  Network().api.getGifs().enqueue(object : Callback<Post> {
              override fun onResponse(call: Call<Post>, response: Response<Post>) {
                  btnRepeat.visibility = View.GONE
                  btnNext.visibility = View.VISIBLE
                  btnPrev.visibility = View.VISIBLE
                  displaySubject(response.body()!!)
                  myList.add(response.body()!!)
              }

              override fun onFailure(call: Call<Post>, t: Throwable) {
                  btnNext.visibility = View.GONE
                  btnPrev.visibility = View.GONE
                  imageGif.setImageResource(R.drawable.errorconnect)
                  description.setText(R.string.error)
                  btnRepeat.visibility = View.VISIBLE
              }
          })*/
    }


    private fun buttonPrevBlock() {
        btnPrev.isEnabled = index != 0
    }

    private fun displaySubject(url:String,desc:String) {
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.loadingicon)
            .error(R.drawable.errorconnect)
            .into(imageGif)
        description.text=desc

    }

   /* private fun displaySubjectX(result: Result) {
        val str:String?=result.result.get(supIndex).gifUrl?.replace("http","https")
        Glide.with(this)
            .load(str)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.loadingicon)
            .error(R.drawable.errorconnect)
            .into(imageGif)
        description.text = result.result.get(supIndex).desc
    }*/

}




