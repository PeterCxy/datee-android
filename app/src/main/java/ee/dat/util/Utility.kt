package ee.dat.util

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import ee.dat.R
import ee.dat.bean.ApiResult
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

// From <https://proandroiddev.com/easy-edittext-content-validation-with-kotlin-316d835d25b3>
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object: TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged.invoke(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
    })
}

fun EditText.validate(validator: (String) -> Boolean, message: String) {
    this.afterTextChanged {
        this.error = if (it.isEmpty() || validator(it)) null else message
    }
    this.error = if (this.text.isEmpty() || validator(this.text.toString())) null else message
}

fun String.isValidEmail(): Boolean
        = this.isNotEmpty() &&
        Patterns.EMAIL_ADDRESS.matcher(this).matches()

// Assert that no EditText in this ViewGroup tree have any pending error
// and are all not empty
fun ViewGroup.assertTextNoErrorAndFilled(): Boolean {
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child is EditText && (child.text.isEmpty() || child.error != null)) {
            child.requestFocus()
            return false
        } else if (child is ViewGroup) {
            if (!child.assertTextNoErrorAndFilled()) return false
        }
    }
    return true
}

fun ViewGroup.findLastEditText(): EditText? {
    var cur: EditText? = null;
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child is EditText) {
            cur = child
        } else if (child is ViewGroup) {
            val e = child.findLastEditText()
            if (e != null) {
                cur = e
            }
        }
    }
    return cur
}

val EditText.str: String
    get() {
        return this.text!!.toString().trim()
    }

data class MaybeResponse<T>(
    val success: Boolean,
    val resp: Response<T>?,
    val err: String?
)

inline fun <T> Call<T>.executeMaybe(): MaybeResponse<T> {
    return try {
        MaybeResponse(true, execute(), null)
    } catch (e: IOException) {
        MaybeResponse(false, null, e.message)
    }
}

inline fun <T> MaybeResponse<ApiResult<T>>.processResult(err: (String?) -> Unit): T? {
    return if (success) {
        this.resp!!.processResult(err)
    } else {
        err(this.err)
        null
    }
}

inline fun <T> Response<ApiResult<T>>.processResult(err: (String?) -> Unit): T? {
    return if (isSuccessful) {
        val body = body()!!
        if (body.ok) {
            body()!!.result
        } else {
            err(body.reason)
            null
        }
    } else {
        try {
            val res: ApiResult<T> =
                Gson().fromJson(errorBody()!!.string(), object : TypeToken<ApiResult<T>>() {}.type)
            err(res.reason)
        } catch (err: JsonSyntaxException) {
            err(null)
        }
        null
    }
}

inline fun Context.showErrorToast(msg: String?) {
    if (msg != null) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(this, R.string.request_failure, Toast.LENGTH_SHORT).show()
    }
}

object Utility {
    fun disableEnableControls(enable: Boolean, vg: ViewGroup) {
        for (i in 0 until vg.childCount) {
            val child = vg.getChildAt(i)
            if (child is ViewGroup) {
                disableEnableControls(enable, child)
            } else {
                child.isEnabled = enable
            }
        }
    }
}