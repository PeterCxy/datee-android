package ee.dat.bean

import com.google.gson.annotations.SerializedName

interface UserTraits {
    val romance: Int
    val openness: Int
    val warmheartedness: Int
}

data class SelfAssessment(
    @SerializedName("romance")
    override val romance: Int,
    @SerializedName("openness")
    override val openness: Int,
    @SerializedName("warmheartedness")
    override val warmheartedness: Int
): UserTraits

data class MatchingPreferences(
    @SerializedName("romance")
    override val romance: Int,
    @SerializedName("openness")
    override val openness: Int,
    @SerializedName("warmheartedness")
    override val warmheartedness: Int,
    @SerializedName("gender")
    val gender: Gender, // TODO: should we consider making this an array? xD
    @SerializedName("minAge")
    val minAge: Int,
    @SerializedName("maxAge")
    val maxAge: Int
): UserTraits