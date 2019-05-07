package ee.dat.api

import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import ee.dat.DateeApplication
import ee.dat.bean.*
import ee.dat.util.*
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface DateeApi {
    companion object {
        // TODO: Change this to non-internal when beta testing
        private const val API_ENDPOINT = "http://192.168.1.115:23456"
        private const val CLIENT_ID = "default"
        private const val CLIENT_SECRET = "123456789"
        val api: DateeApi by lazy {
            val retrofit = with(Retrofit.Builder()) {
                baseUrl(API_ENDPOINT)
                addConverterFactory(GsonConverterFactory.create())
                client(buildHttpClient())
                build()
            }
            retrofit.create(DateeApi::class.java)
        }
        val picasso: Picasso by lazy {
            with(Picasso.Builder(DateeApplication.context)) {
                downloader(OkHttp3Downloader(buildHttpClient()))
                build()
            }
        }

        private fun buildHttpClient(): OkHttpClient =
            with(OkHttpClient.Builder()) {
                addInterceptor {
                    // Add Authorization header if accessToken is present
                    if (LoginStateManager.accessToken != null) {
                        it.proceed(with(it.request().newBuilder()) {
                            addHeader("Authorization", "Bearer ${LoginStateManager.accessToken}")
                            build()
                        })
                    } else {
                        it.proceed(it.request())
                    }
                }
                build()
            }

        fun buildPhotoUrl(id: String): String = "$API_ENDPOINT/photos/$id"
    }

    // === Auth APIs ===
    @FormUrlEncoded
    @POST("auth/token")
    fun authLogin(
        @Field("scope")
        scope: String = "default",
        @Field("client_id")
        clientId: String = CLIENT_ID,
        @Field("client_secret")
        clientSecret: String = CLIENT_SECRET,
        @Field("grant_type")
        grantType: String = "password",
        @Field("username")
        username: String,
        @Field("password")
        password: String
    ): Call<OAuthResponse>

    @FormUrlEncoded
    @POST("auth/token")
    fun authRefresh(
        @Field("scope")
        scope: String = "default",
        @Field("client_id")
        clientId: String = CLIENT_ID,
        @Field("client_secret")
        clientSecret: String = CLIENT_SECRET,
        @Field("grant_type")
        grantType: String = "refresh_token",
        @Field("refresh_token")
        refresh_token: String
    ): Call<OAuthResponse>

    // === User APIs ===
    @PUT("user/register")
    fun register(@Body info: RegisterUserInfo): Call<ApiResult<Void>>
    @GET("user/whoami")
    fun whoami(): Call<ApiResult<User>>
    @PUT("user/self_assessment")
    fun setSelfAssessment(@Body assessment: SelfAssessment): Call<ApiResult<Void>>
    @PUT("user/matching_pref")
    fun setMatchingPrefs(@Body prefs: MatchingPreferences): Call<ApiResult<Void>>
    @GET("user/random")
    fun randomUser(): Call<ApiResult<String>>
    @GET("user/{uid}")
    fun getUser(@Path("uid") uid: String): Call<ApiResult<BaseUserInfo>>

    // === Photo APIs ===
    @Multipart
    @PUT("photos/upload")
    // Actual return type should be Photo, but of no use here
    fun uploadPhoto(@Part file: MultipartBody.Part): Call<ApiResult<Void>>
    @GET("photos/list/{uid}")
    fun listPhotos(@Path("uid") uid: String): Call<ApiResult<List<String>>>

    // === Rating APIs ===
    @FormUrlEncoded
    @PUT("rate/{uid}")
    fun rateUser(@Path("uid") uid: String, @Field("score") score: Int): Call<ApiResult<Void>>

    // === Match APIs ===
    @GET("match/current")
    fun getMatchedUid(): Call<ApiResult<String>>
    @DELETE("match/current")
    fun rejectCurrentMatch(): Call<ApiResult<Void>>
}