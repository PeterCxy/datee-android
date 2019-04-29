package ee.dat.bean

import com.google.gson.annotations.SerializedName

data class ApiResult<T>(
    @SerializedName("ok")
    val ok: Boolean,
    @SerializedName("result")
    val result: T?,
    @SerializedName("reason")
    val reason: String?
)