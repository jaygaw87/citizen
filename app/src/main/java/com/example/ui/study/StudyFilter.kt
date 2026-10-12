package com.example.ui.study

/**
 * Filter options for studying civics questions.
 */
enum class StudyFilter(val displayName: String, val shortLabel: String) {
    ALL("All", "All Questions"),
    GOVERNMENT("American Government", "Government"),
    HISTORY("American History", "History"),
    CIVICS("Integrated Civics", "Civics"),
    SENIOR_65_20("65/20", "65/20"),
    FAVORITES("Favorites", "Saved"),
    NEEDS_PRACTICE("Needs Practice", "Needs Practice");

    companion object {
        fun fromName(name: String?): StudyFilter {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: ALL
        }
    }
}
