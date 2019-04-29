package ee.dat.api

import ee.dat.bean.ApiResult
import ee.dat.bean.RegisterUserInfo
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.PUT

interface DateeApi {
    companion object {
        // TODO: Change this to non-internal when beta testing
        private const val API_ENDPOINT = "http://192.168.1.115:23456"
        val api: DateeApi by lazy {
            val retrofit = with(Retrofit.Builder()) {
                baseUrl(API_ENDPOINT)
                addConverterFactory(GsonConverterFactory.create())
                build()
            }
            retrofit.create(DateeApi::class.java)
        }
    }

    @PUT("user/register")
    fun register(@Body info: RegisterUserInfo): Call<ApiResult<Void>>
}