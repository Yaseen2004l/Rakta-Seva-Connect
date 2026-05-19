# Rakta-Seva Connect 🩸

An emergency blood donor connection Android app built with Kotlin, Jetpack Compose, Firebase, and Claude AI.

## Features
- Register as a blood donor with blood group and location
- Post emergency blood requests that notify nearby donors instantly
- Accept or decline donation requests
- Auto 90-day cooldown after donation
- Donor privacy — phone number only revealed after accepting
- AI generated urgent appeal messages using Claude (Anthropic)
- Glass morphism dark UI

## Setup Instructions
1. Clone the repository
2. Open in Android Studio
3. Add your `google-services.json` from Firebase Console into the `app/` folder
4. Create `AnthropicClient.kt` inside `app/src/main/java/com/raktaseva/app/api/`
5. Add your Anthropic API key inside that file
6. Run the app on Android 9.0 or above

## Tech Stack
- Kotlin + Jetpack Compose
- Firebase Firestore + Firebase Cloud Messaging
- Anthropic Claude API
- MVVM Architecture
- OkHttp + Gson

## Built By
Mohit Pandit — 1VI22CS063
MindMatrix - Android App Development using Gen AI — Android Internship
