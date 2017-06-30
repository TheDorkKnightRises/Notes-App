# Notes-App
A simple Material Design note-taking experience for Android

## Features
- Write notes having a title, subtitle and some content
- Share notes
- Create notifications from notes
- Archive notes
- Backup notes to Google Drive
- Homescreen widget
- Notification shortcut for creating new note
- App Shortcuts (on Android 7.1+)
- Create notes using "OK Google" voice actions
- Optional light theme

## Instructions for building
This app uses the Google Drive API to backup and restore databases. The Drive API requires you to authorize your app with OAuth 2.0
 credentials based on your application's signing keys. Enable the Drive API in the [Google Developer Console](https://console.developers.google.com/apis/) and generate these credentials by providing the SHA-1 for your signing key.

NOTE: You will need to edit the signing configs in the app level build.gradle

## Contributing
Contributions are welcome via pull requests. By contributing, you agree to grant the developer the non-exclusive rights to view, modify and redistribute your code without any restrictions.

## Licence
     Copyright © 2016-2017  Samriddha Basu
     You may view, modify, use and distribute this code and any derived works, subject to the conditions
    - You must give appropriate credit, retain all copyright notices, provide a link to the original work and license (if possible), and indicate if changes were made
    - You may not use the material for commercial purposes (without the explicit permission of the original author)
    - The original author will not be held liable for any misuse, or direct or indirect harm caused to any person(s).
	  See LICENSE.txt file for more info
