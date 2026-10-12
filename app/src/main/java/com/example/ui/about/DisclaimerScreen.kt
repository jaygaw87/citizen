package com.example.ui.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppTopBar

@Composable
fun DisclaimerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.testTag("disclaimer_screen"),
        topBar = {
            AppTopBar(
                title = "Disclaimer",
                showCivicEmblem = false,
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Independent Educational Application",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "This application is an independent educational tool. It is not affiliated with, endorsed by, sponsored by, or connected to U.S. Citizenship and Immigration Services (USCIS) or any U.S. government agency.",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No Government Affiliation or Warranty",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "This app is provided solely for educational preparation and self-study purposes. Passing simulated practice tests or flashcard reviews within this application does not guarantee passing the official civics test administered during your naturalization interview by a USCIS officer.",
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Consult Official Resources",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Users must consult official USCIS resources for the most current test requirements, policies, designated study materials, and naturalization interview guidelines. Specific elected officials (such as governors, U.S. senators, and U.S. representatives) and capitals may change depending on your state of residence and election results.\n\nOfficial USCIS information is available at:\nhttps://www.uscis.gov/citizenship",
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Intellectual Property Notice",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Civics questions and answers are public information published by the federal government. All other design elements, layout structures, and software architecture are independent creative works.",
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
