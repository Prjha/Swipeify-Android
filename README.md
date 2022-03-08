# Android-Swipeify
Android app that uses Spotify API to recommend new music based on your listening history. Written in Kotlin and uses Spotify Web API and Android SDK. New music is presented in swipe cards where a left swipe plays the next song and a right swipe can add the app to your liked songs in spotify.

The app connects to Spotify app on device using Android SDK to able to pause, play and change songs. Two  Spotify Web API endpoints are used. The first is to get your top tracks. The second is uses to get recommendation based on the top tracks. These new recommendations are presented to you in the app.

To use the API, OAuth Tokens are needed. The API request needs to include this token. To get the token, the app uses the Spotify SDK Authorization Client.

#### Demo Video
https://streamable.com/hwx2gj
