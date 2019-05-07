package ee.dat.bean

import com.google.gson.annotations.SerializedName

enum class Gender(val value: Int) {
    @SerializedName("0")
    Male(0),
    @SerializedName("1")
    Female(1)
}

enum class Country(val value: Int) {
    @SerializedName("0")
    China(0)
}

enum class City(val value: Int, val country: Country) {
    @SerializedName("0")
    Suzhou(0, Country.China)
}

enum class State(val value: Int) {
    @SerializedName("0")
    Registered(0),
    @SerializedName("1")
    PhotoUploaded(1),
    @SerializedName("2")
    SelfAssessmentDone(2),
    @SerializedName("3")
    MatchingPreferencesSet(3),
    @SerializedName("4")
    Idle(4),
    @SerializedName("5")
    Matched(5)
    // TODO: Finish all the states
}

interface UserInfo {
    val email: String
    val firstName: String
    val lastName: String
    val age: Int
    val gender: Gender
    val country: Country
    val city: City
}

data class BaseUserInfo(
    @SerializedName("email")
    override val email: String,
    @SerializedName("firstName")
    override val firstName: String,
    @SerializedName("lastName")
    override val lastName: String,
    @SerializedName("age")
    override val age: Int,
    @SerializedName("gender")
    override val gender: Gender,
    @SerializedName("country")
    override val country: Country,
    @SerializedName("city")
    override val city: City
): UserInfo

data class RegisterUserInfo(
    // === Shared in UserInfo interface ===
    @SerializedName("email")
    override val email: String,
    @SerializedName("firstName")
    override val firstName: String,
    @SerializedName("lastName")
    override val lastName: String,
    @SerializedName("age")
    override val age: Int,
    @SerializedName("gender")
    override val gender: Gender,
    @SerializedName("country")
    override val country: Country,
    @SerializedName("city")
    override val city: City,
    // === Specific to this variation ===
    @SerializedName("password")
    val password: String
): UserInfo

data class User(
    // === Shared in UserInfo interface ===
    @SerializedName("email")
    override val email: String,
    @SerializedName("firstName")
    override val firstName: String,
    @SerializedName("lastName")
    override val lastName: String,
    @SerializedName("age")
    override val age: Int,
    @SerializedName("gender")
    override val gender: Gender,
    @SerializedName("country")
    override val country: Country,
    @SerializedName("city")
    override val city: City,
    // === Specific to this variation ===
    @SerializedName("uid")
    val uid: String,
    @SerializedName("state")
    val state: State
): UserInfo