package com.RivskyInc.movienow


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.RivskyInc.movienow.API_SERVICE.ApiFactory
import com.RivskyInc.movienow.API_SERVICE.Result
import com.RivskyInc.movienow.adapter.MoviesAdapter
import com.RivskyInc.movienow.database.MovieDao
import com.RivskyInc.movienow.database.MovieDataBase
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import pl.droidsonroids.gif.GifImageView
import java.util.*
import kotlin.random.Random

class FindMovieActivity : AppCompatActivity() {

    private lateinit var buttonNextMovie: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MoviesAdapter
    private lateinit var viewModel: MainViewModel
    private lateinit var dataBase: MovieDataBase
    private lateinit var movieDao: MovieDao
    private lateinit var loadGif : GifImageView
    private lateinit var clickTip : LottieAnimationView
    private val buttonClick = AlphaAnimation(1f, 0.6f)
    val TAG = "FindMovieActivity"
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_find_movie)

        initViewFindMovie()

        MobileAds.initialize(this@FindMovieActivity)



        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                adError?.toString()?.let { Log.d(TAG, it) }
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
        clickTip.postDelayed(Runnable { clickTip.setVisibility(View.VISIBLE) }, 2000)
        clickTip.postDelayed(Runnable { clickTip.setVisibility(GONE) }, 5000)

        recyclerView.postDelayed(Runnable { recyclerView.setVisibility(View.VISIBLE) }, 1100)
        val handler = Handler()

        loadGif.postDelayed(Runnable { loadGif.setVisibility(GONE) }, 1100)
       // handler.postDelayed(Runnable { loadGif.setVisibility(View.VISIBLE) }, 5000)

        loadMovies()
        recyclerView.layoutManager = LinearLayoutManager(           // -> for future catalog

            this,
            LinearLayoutManager.HORIZONTAL, false
        )

        recyclerView.adapter = adapter

        adapter.onMovieClickListener_object = object : MoviesAdapter.onMovieClickListener {
            override fun onMovieClick(movie: Result) {
                val  intent = MovieDetailActivity.NewIntent(this@FindMovieActivity, movie).also {

                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

                   // intent.putExtra("movie.title", movie.title)

                }
//                    intent.putExtra(EXTRA_MOVIE,movie.release_date)
//                    intent.putExtra(EXTRA_MOVIE, movie.id)
//                    intent.putExtra(EXTRA_MOVIE, movie.overview)
//                    intent.putExtra(EXTRA_MOVIE, movie.title)
//                    intent.putExtra(EXTRA_MOVIE, movie.poster_path)
//                    intent.putExtra(EXTRA_MOVIE, movie.release_date)
//                    intent.putExtra(EXTRA_MOVIE, movie.vote_average)
                startActivity(intent)
            }

        }
        val animButton = AnimationUtils.loadAnimation(this, com.google.android.material.R.anim.mtrl_bottom_sheet_slide_out)
        buttonNextMovie.setOnClickListener {
            buttonNextMovie.startAnimation(animButton)
           //onClick(buttonNextMovie)

            loadGif.setVisibility(View.VISIBLE)
            loadGif.postDelayed(Runnable { loadGif.setVisibility(GONE) }, 800)
            recyclerView.setVisibility(GONE)
            recyclerView.postDelayed(Runnable { recyclerView.setVisibility(View.VISIBLE) }, 800)

            loadMovies()


        }

    }


    private fun initViewFindMovie() {
        clickTip = findViewById(R.id.click_tip)
        buttonNextMovie = findViewById(R.id.buttonNextMovie)
        recyclerView = findViewById(R.id.recyclerView)
        loadGif = findViewById<GifImageView>(R.id.gifLoadMovies)
        adapter = MoviesAdapter()

    }


    fun loadMovies() {

        val showResults = adapter.itemCount.toString()

        Log.d("TEST ITEMS", showResults.toString())

        val vote_min = 500
        val vote_max = 1000000

        var setupGenre: Int? = intent.getIntExtra(GENRE, 0)
        Log.d("REAL INTENT  GENRE ", setupGenre.toString())

        var  setupAverageMin = intent.getIntExtra("averageMin", 5 )
        var setupAverageMax = intent.getIntExtra("averageMax", 10)
       // val setupPageForAllGenre = intent.getStringExtra("setupPageForAllGenre")?.toInt()
        // val setupPageForAllGenres = intent.getStringExtra("genre")?.toInt() // use when genre is null

        val voteAverageMin = setupAverageMin
        val voteAverageMax = setupAverageMax
        var genre: Int? = setupGenre
        Log.d("GENRE ", setupGenre.toString())
        Log.d("real genre ", genre.toString())
        var region: String = Locale.getDefault().country.toString()
        var page = 0

        if (genre?.equals(27) == true) {
           buttonNextMovie.setImageResource(R.drawable.button_horror)      // horror
        } else if (genre?.equals(28) == true) {
            buttonNextMovie.setImageResource(R.drawable.button_action)        // action
        } else if (genre?.equals(35) == true) {
           buttonNextMovie.setImageResource(R.drawable.button_comedy)        //comedy
        } else if (genre?.equals(18) == true) {
           buttonNextMovie.setImageResource(R.drawable.button_drama)       //drama
        } else if (genre?.equals(14) == true) {
          buttonNextMovie.setImageResource(R.drawable.fantasy_button)       //fantasy
        } else if (genre?.equals(53) == true) {
            buttonNextMovie.setImageResource(R.drawable.button_knife)       //thriller
        } else if (genre?.equals(16) == true) {
           buttonNextMovie.setImageResource(R.drawable.button_cartoons)        //animation
            // all
        } else {
            genre = null
            buttonNextMovie.setImageResource(R.drawable.button_start_v2)
        }
        if (region.equals("UA")){                           // Algorithm for
                                                            //  Ukrainian localization
              page  =
                if (genre?.equals(27) == true) {
                    Random.nextInt(1, 10)       // horror
                } else if (genre?.equals(28) == true) {
                    Random.nextInt(1, 21)         // action
                } else if (genre?.equals(35) == true) {
                    Random.nextInt(1, 19)        //comedy
                } else if (genre?.equals(18) == true) {
                    Random.nextInt(1, 25)        //drama
                } else if (genre?.equals(14) == true) {
                    Random.nextInt(1, 9)        //fantasy
                } else if (genre?.equals(53) == true) {
                    Random.nextInt(1, 20)        //thriller
                } else if (genre?.equals(16) == true) {
                    Random.nextInt(1, 7)        //animation
                    // all
                } else {
                    genre = null
                    Random.nextInt(1, 63)
                }
        } else{
            page  =
                try {


                    if (genre?.equals(27) == true) {
                        Random.nextInt(1, 41)       // horror
                    } else if (genre?.equals(28) == true) {
                        Random.nextInt(1, 40)         // action
                    } else if (genre?.equals(35) == true) {
                        Random.nextInt(1, 40)        //comedy
                    } else if (genre?.equals(18) == true) {
                        Random.nextInt(1, 40)        //drama
                    } else if (genre?.equals(14) == true) {
                        Random.nextInt(1, 37)        //fantasy
                    } else if (genre?.equals(53) == true) {
                        Random.nextInt(1, 40)        //thriller
                    } else if (genre?.equals(16) == true) {
                        Random.nextInt(1, 27)        //animation
                        // all
                    } else {
                        genre = null
                        Random.nextInt(1, 100)
                    }


                } catch (e: Exception) {
                    Log.d("EXCEPTION : ", e.message.toString())
                }
        }



//        if (genre == null) {
//            page = Random.nextInt(1, 100)       //all
//        }


        var language: String = Locale.getDefault().language.toString()
        if (language.equals("ru")) {
            language = "uk"
        } else {
            language
        }

        Log.d("Region: ", region.toString())
        Log.d("Language: ", language.toString())
        // Log.d("Region:  ,", region.toString())
        // need more total results, something get wrong and results get less than must be
        // fixed, the region's fault

        //need to fix getStringExtra -> getIntExtra
       // val video = true

        val apiFactory = ApiFactory.apiService.loadMovies(
            page,
            language,
             region,
            vote_min,
            vote_max,
            genre,
            voteAverageMin,
            voteAverageMax,

        )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {

                Log.d("Test", it.toString())

                val dataMovies = adapter.setMovies(it.results)


            }, {
                loadMovies()
                Log.d("Test_Fail", it.message.toString())
            })

    }

    private fun onClick(v: View) {
        v.startAnimation(buttonClick)
    }
}
