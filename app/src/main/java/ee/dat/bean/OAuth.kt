package ee.dat.bean

import com.google.gson.annotations.SerializedName

data class OAuthResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("expires_in")
    val expiresIn: Long,
    @SerializedName("refresh_token")
    val refreshToken: String
)