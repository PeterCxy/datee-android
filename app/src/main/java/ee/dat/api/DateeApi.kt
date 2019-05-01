package ee.dat.api

import ee.dat.bean.ApiResult
import ee.dat.bean.OAuthResponse
import ee.dat.bean.RegisterUserInfo
import ee.dat.bean.User
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
            val httpClient = with(OkHttpClient.Builder()) {
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

            val retrofit = with(Retrofit.Builder()) {
                baseUrl(API_ENDPOINT)
                addConverterFactory(GsonConverterFactory.create())
                client(httpClient)
                build()
            }
            retrofit.create(DateeApi::class.java)
        }
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

    // === Photo APIs ===
    @Multipart
    @PUT("photos/upload")
    // Actual return type should be Photo, but of no use here
    fun uploadPhoto(@Part file: MultipartBody.Part): Call<ApiResult<Void>>
    @GET("photos/list/{uid}")
    fun listPhotos(@Path("uid") uid: String): Call<ApiResult<List<String>>>
}