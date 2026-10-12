package com.example.speech

import java.util.Locale

enum class MatchStatus {
    CORRECT,
    LIKELY_CORRECT,
    NEEDS_REVIEW,
    NO_MATCH
}

data class SpeechMatchResult(
    val status: MatchStatus,
    val userSpokenText: String,
    val matchedAnswer: String? = null,
    val explanation: String
) {
    val isAffirmative: Boolean
        get() = status == MatchStatus.CORRECT || status == MatchStatus.LIKELY_CORRECT
}

object AnswerMatcher {

    private val STOP_WORDS = setOf(
        "the", "a", "an", "and", "or", "to", "of", "in", "for", "on", "with",
        "is", "are", "was", "were", "it", "its", "they", "their", "that", "this"
    )

    private val NUMBER_WORDS = mapOf(
        "zero" to "0", "one" to "1", "two" to "2", "three" to "3", "four" to "4",
        "five" to "5", "six" to "6", "seven" to "7", "eight" to "8", "nine" to "9",
        "ten" to "10", "twelve" to "12", "thirteen" to "13", "fourteen" to "14",
        "fifteen" to "15", "twenty" to "20", "twenty seven" to "27", "fifty" to "50",
        "one hundred" to "100", "435" to "four hundred thirty five", "100" to "one hundred"
    )

    /**
     * Normalizes an answer string by trimming, lowercasing, stripping punctuation,
     * normalizing common numeral words, and condensing whitespace.
     */
    fun normalize(text: String): String {
        if (text.isBlank()) return ""
        var cleaned = text.lowercase(Locale.US)
            // Replace punctuation with spaces
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .trim()

        // Replace number phrases
        NUMBER_WORDS.forEach { (word, digit) ->
            cleaned = cleaned.replace(Regex("\\b$word\\b"), digit)
        }

        // Collapse multiple spaces
        cleaned = cleaned.replace(Regex("\\s+"), " ").trim()
        return cleaned
    }

    /**
     * Strips leading conversational phrases like "it is", "they are", "i think it is", "the".
     */
    fun stripConversationalPrefixes(text: String): String {
        var str = text.trim()
        val prefixes = listOf(
            "i think it is ",
            "i believe it is ",
            "the answer is ",
            "it is ",
            "it s ",
            "they are ",
            "we have ",
            "there are ",
            "the "
        )
        for (prefix in prefixes) {
            if (str.startsWith(prefix)) {
                str = str.removePrefix(prefix).trim()
            }
        }
        return str
    }

    /**
     * Evaluates a spoken user phrase against a list of USCIS accepted answers.
     */
    fun matchAnswer(spokenText: String, acceptedAnswers: List<String>): SpeechMatchResult {
        if (spokenText.isBlank()) {
            return SpeechMatchResult(
                status = MatchStatus.NO_MATCH,
                userSpokenText = spokenText,
                explanation = "No speech detected. Please tap the microphone and try again."
            )
        }

        val normSpoken = normalize(spokenText)
        val strippedSpoken = stripConversationalPrefixes(normSpoken)

        if (normSpoken.isBlank() && strippedSpoken.isBlank()) {
            return SpeechMatchResult(
                status = MatchStatus.NO_MATCH,
                userSpokenText = spokenText,
                explanation = "Could not recognize an answer. Please try again."
            )
        }

        val spokenTokens = normSpoken.split(" ").filter { it.isNotBlank() }
        val strippedTokens = strippedSpoken.split(" ").filter { it.isNotBlank() }
        val spokenKeyTokens = strippedTokens.filterNot { it in STOP_WORDS }.toSet()

        var bestMatchAnswer: String? = null
        var highestStatus = MatchStatus.NO_MATCH

        for (accepted in acceptedAnswers) {
            val normAccepted = normalize(accepted)
            val strippedAccepted = stripConversationalPrefixes(normAccepted)
            val acceptedTokens = normAccepted.split(" ").filter { it.isNotBlank() }
            val strippedAcceptedTokens = strippedAccepted.split(" ").filter { it.isNotBlank() }
            val acceptedKeyTokens = strippedAcceptedTokens.filterNot { it in STOP_WORDS }.toSet()

            // 1. Exact match
            if (normSpoken == normAccepted || strippedSpoken == strippedAccepted) {
                return SpeechMatchResult(
                    status = MatchStatus.CORRECT,
                    userSpokenText = spokenText,
                    matchedAnswer = accepted,
                    explanation = "Matches accepted answer: \"$accepted\"."
                )
            }

            // 2. Short answer match (e.g. spoken "the constitution" vs accepted "Constitution", or spoken "constitution" vs accepted "the Constitution")
            if (strippedSpoken == normAccepted || normSpoken == strippedAccepted) {
                return SpeechMatchResult(
                    status = MatchStatus.CORRECT,
                    userSpokenText = spokenText,
                    matchedAnswer = accepted,
                    explanation = "Matches accepted answer: \"$accepted\"."
                )
            }

            // 3. Substring containment for whole phrases
            if (normSpoken.contains(normAccepted) || (normAccepted.length > 4 && normSpoken.contains(strippedAccepted))) {
                if (highestStatus != MatchStatus.CORRECT) {
                    highestStatus = MatchStatus.LIKELY_CORRECT
                    bestMatchAnswer = accepted
                }
            } else if (normAccepted.contains(strippedSpoken) && strippedSpoken.length >= 4 && !STOP_WORDS.contains(strippedSpoken)) {
                // Spoken is a substantial part of the accepted answer (e.g. spoken "freedom of speech" in "freedom of speech, press, religion...")
                if (highestStatus.ordinal > MatchStatus.LIKELY_CORRECT.ordinal) {
                    highestStatus = MatchStatus.LIKELY_CORRECT
                    bestMatchAnswer = accepted
                }
            }

            // 4. Key Token Overlap
            if (acceptedKeyTokens.isNotEmpty()) {
                val intersection = spokenKeyTokens.intersect(acceptedKeyTokens)
                val overlapRatioAccepted = intersection.size.toFloat() / acceptedKeyTokens.size.toFloat()
                val overlapRatioSpoken = if (spokenKeyTokens.isNotEmpty()) {
                    intersection.size.toFloat() / spokenKeyTokens.size.toFloat()
                } else 0f

                if (overlapRatioAccepted >= 0.75f && overlapRatioSpoken >= 0.5f) {
                    if (highestStatus.ordinal > MatchStatus.LIKELY_CORRECT.ordinal) {
                        highestStatus = MatchStatus.LIKELY_CORRECT
                        bestMatchAnswer = accepted
                    }
                } else if (overlapRatioAccepted >= 0.5f || (intersection.isNotEmpty() && acceptedKeyTokens.size <= 2)) {
                    if (highestStatus.ordinal > MatchStatus.NEEDS_REVIEW.ordinal) {
                        highestStatus = MatchStatus.NEEDS_REVIEW
                        bestMatchAnswer = accepted
                    }
                }
            }
        }

        return when (highestStatus) {
            MatchStatus.CORRECT -> SpeechMatchResult(
                status = MatchStatus.CORRECT,
                userSpokenText = spokenText,
                matchedAnswer = bestMatchAnswer,
                explanation = "Matches accepted answer."
            )
            MatchStatus.LIKELY_CORRECT -> SpeechMatchResult(
                status = MatchStatus.LIKELY_CORRECT,
                userSpokenText = spokenText,
                matchedAnswer = bestMatchAnswer,
                explanation = "Your spoken answer closely matches \"${bestMatchAnswer ?: "an accepted answer"}\"."
            )
            MatchStatus.NEEDS_REVIEW -> SpeechMatchResult(
                status = MatchStatus.NEEDS_REVIEW,
                userSpokenText = spokenText,
                matchedAnswer = bestMatchAnswer,
                explanation = "Your answer may be correct. Review the official accepted answers."
            )
            MatchStatus.NO_MATCH -> SpeechMatchResult(
                status = MatchStatus.NO_MATCH,
                userSpokenText = spokenText,
                explanation = "Does not appear to match the accepted answers. Check the answers below or try again."
            )
        }
    }

    /**
     * High-level evaluation convenience method returning match boolean and matched answer.
     */
    fun evaluateAnswer(spokenText: String, acceptedAnswers: List<String>): EvaluationResult {
        val result = matchAnswer(spokenText, acceptedAnswers)
        return EvaluationResult(
            isMatch = result.isAffirmative,
            matchedAcceptedAnswer = result.matchedAnswer,
            status = result.status
        )
    }

    data class EvaluationResult(
        val isMatch: Boolean,
        val matchedAcceptedAnswer: String?,
        val status: MatchStatus
    )
}
