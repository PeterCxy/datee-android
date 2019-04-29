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
    private const val PREF_REFRESH_TOKEN_VALID_UNTIL = "refresh_token_valid_until"

    fun getLoginState(): LoginState {
        if (LocalStorageManager.getString(
                PREF_ACCESS_TOKEN) == null) {
            return LoginState.LoggedOut
        }
        val now = System.currentTimeMillis()
        if (LocalStorageManager.getLong(
                PREF_ACCESS_TOKEN_VALID_UNTIL) >= now) {
            if (LocalStorageManager.getString(
                    PREF_REFRESH_TOKEN) != null) {
                if (LocalStorageManager.getLong(
                        PREF_REFRESH_TOKEN_VALID_UNTIL) < now) {
                    return LoginState.NeedRefresh
                } else {
                    return LoginState.LoggedOut
                }
            } else {
                return LoginState.LoggedOut
            }
        } else {
            return LoginState.LoggedIn
        }
    }
}