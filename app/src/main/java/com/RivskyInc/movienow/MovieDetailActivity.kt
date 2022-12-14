package com.RivskyInc.movienow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.RivskyInc.movienow.API_SERVICE.BASE_IMAGE_POSTER
import com.RivskyInc.movienow.API_SERVICE.Result
import com.RivskyInc.movienow.adapter.MoviesAdapter
import com.bumptech.glide.Glide
import com.google.android.gms.ads.*
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

const val EXTRA_MOVIE: String = "movie"
const val GENRE : String = "genre"
class MovieDetailActivity : AppCompatActivity(){

    private lateinit var posterImageView: ImageView
    private lateinit var nameOfMovie: TextView
    private lateinit var yearOfMovie: TextView
    private lateinit var descriptionOfMovie: TextView
    private lateinit var rating: TextView
    private lateinit var adapter: MoviesAdapter
    private lateinit var backButton : Button
    private var mRewardedAd: RewardedAd? = null
    private val TAG = "MovieDetailActivity"
    // private lateinit var movieDao : MovieDao
    private lateinit var imageFavorite: ImageButton
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        initView()

       // MobileAds.initialize(this@MovieDetailActivity)

//            var adRequest = AdRequest.Builder().build()
//            RewardedAd.load(
//                this@MovieDetailActivity,
//                "ca-app-pub-3940256099942544/5224354917",
//                adRequest,
//                object : RewardedAdLoadCallback() {
//                    override fun onAdFailedToLoad(adError: LoadAdError) {
//                        adError?.toString()?.let { Log.d(TAG, it) }
//                        mRewardedAd = null
//                    }
//
//                    override fun onAdLoaded(rewardedAd: RewardedAd) {
//                        Log.d(TAG, "Ad was loaded.")
//                        mRewardedAd = rewardedAd
//                    }
//                })
//            if (mRewardedAd != null) {
//                mRewardedAd?.show(this, OnUserEarnedRewardListener() {
//                    fun onUserEarnedReward(rewardItem: RewardItem) {
//                        var rewardAmount = rewardItem.amount
//                        var rewardType = rewardItem.type
//                        Log.d(TAG, "User earned the reward.")
//                    }
//                })
//            } else {
//                Log.d(TAG, "The rewarded ad wasn't ready yet.")
//            }

        val setupGenre: Int? = intent.getIntExtra(GENRE, 0)
        val setupAverageMin = intent.getIntExtra("averageMin", 5 )
        val setupAverageMax = intent.getIntExtra("averageMax", 10)
        var  genre = setupGenre
        Log.d("testGenre", setupGenre.toString())
        backButton.setOnClickListener {

           // val setupGenre: Int? = intent.getIntExtra("genre", 0)
//            val setupAverageMin = intent.getIntExtra("averageMin", 5 )
//            val setupAverageMax = intent.getIntExtra("averageMax", 10)
//           val intent  = Intent(this, FindMovieActivity::class.java)
//            intent.putExtra(GENRE, setupGenre)
//            startActivity(intent )
        finish()


        }

        var movie: Result? = null
        var movieId: Int? = intent.getIntExtra("movie.id", 0)
        var movieTitle = intent.getStringExtra("movie.title")
        var movieDescription = intent.getStringExtra("movie.overview")
        var movieRating = intent.getDoubleExtra("movie.rating", 0.0)
        var moviePoster = intent.getStringExtra("movie.poster")
        var movieYear = intent.getStringExtra("movie.year")



        Log.d("MOVIE_ID", movieId.toString())
        // var movieInfo  = movieId; movieTitle; movieDescription; movieRating; movieRating; moviePoster; movieYear as? Result
        // Log.d("MOVIE_INFO", movieInfo.toString())
        //  Log.d("MOVIE_TITLE",movie?.title.toString())
        //  Log.d("MOVIE_DESCRIPTION", movieDescription?.overview.toString())
        // Log.d("MOVIE_RATING", movieRating?.vote_average.toString())

        Glide.with(this).load(BASE_IMAGE_POSTER + moviePoster).into(posterImageView)
        nameOfMovie.text = movieTitle
        yearOfMovie.text = movieYear.toString()
        descriptionOfMovie.text = movieDescription
        rating.text = movieRating.toString()



        viewModel = MainViewModel(application)
        viewModel.getFavouriteMovie(movieId)?.observe(this, Observer {

            if (it?.id == null) {
                imageFavorite.setImageResource(R.drawable.heart_icon_empty_resize)

                imageFavorite.setOnClickListener {
                    if (movieId != null) {
                        viewModel.insertMovie(
                            Result(
                                movieId,
                                movieDescription,
                                moviePoster,
                                movieYear,
                                movieTitle,
                                movieRating
                            )
                        )
                    }
                    Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show()

                }
            } else {
                imageFavorite.setImageResource(R.drawable.heart_button_red)
                imageFavorite.setOnClickListener {
                    viewModel.removeMovie(movieId)
                    Toast.makeText(this, "deleted", Toast.LENGTH_SHORT).show()
                }
            }
        })


    }


    companion object {
        fun NewIntent(context: Context, movie: Result): Intent {

            var intent: Intent = Intent(context, MovieDetailActivity::class.java)
            //   intent.putExtra("movie", movie)
            intent.putExtra("movie.id", movie.id)
            intent.putExtra("movie.overview", movie.overview)
            intent.putExtra("movie.title", movie.title)
            intent.putExtra("movie.poster", movie.poster_path)
            intent.putExtra("movie.year", movie.release_date)
            intent.putExtra("movie.rating", movie.vote_average)
            return intent
        }
    }


    private fun initView() {
        posterImageView = findViewById(R.id.posterImageDetail)
        nameOfMovie = findViewById(R.id.nameOfMovieDetail)
        yearOfMovie = findViewById(R.id.yearOfMovieDetail)
        descriptionOfMovie = findViewById(R.id.descriptionOfMovieDetail)
        rating = findViewById(R.id.ratingDetail)
        imageFavorite = findViewById(R.id.imageButtonFavor)
        backButton = findViewById(R.id.buttonBackFromDetail)
    }


}