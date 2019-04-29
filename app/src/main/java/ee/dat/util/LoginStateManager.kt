package ee.dat.util

object LoginStateManager {
    enum class LoginState {
        LoggedOut,
        LoggedIn,
        NeedRefresh
    }

    private const val PREF_ACCESS_TOKEN = "access_token"
    private const val PREF_ACCESS_TOKEN_VALID_UNTIL = "access_token_valid_until"
    private const val PREF_REFRESH_TOKEN = "refresh_token"

    val accessToken: String?
        get() = if (LocalStorageManager.getLong(PREF_ACCESS_TOKEN_VALID_UNTIL)
            >= System.currentTimeMillis()) {
            null
        } else {
            LocalStorageManager.getString(PREF_ACCESS_TOKEN)
        }

    fun setAccessToken(token: String, validUntil: Long) {
        with (LocalStorageManager) {
            putString(PREF_ACCESS_TOKEN, token)
            putLong(PREF_ACCESS_TOKEN_VALID_UNTIL, validUntil)
        }
    }

    var refreshToken: String?
        get() = LocalStorageManager.getString(PREF_REFRESH_TOKEN)
        set(value) = LocalStorageManager.putString(PREF_REFRESH_TOKEN, value!!)

    val loginState: LoginState
        get() {
            if (LocalStorageManager.getString(
                    PREF_ACCESS_TOKEN
                ) == null
            ) {
                return LoginState.LoggedOut
            }
            val now = System.currentTimeMillis()
            if (LocalStorageManager.getLong(
                    PREF_ACCESS_TOKEN_VALID_UNTIL
                ) >= now
            ) {
                if (LocalStorageManager.getString(
                        PREF_REFRESH_TOKEN
                    ) != null
                ) {
                    return LoginState.NeedRefresh
                } else {
                    return LoginState.LoggedOut
                }
            } else {
                return LoginState.LoggedIn
            }
        }
}