# Notes-App
A simple note-taking app for Android that is fully open-source

## Features
- Write notes having a title, subtitle and some content
- Share note
- Create a notification from note
- Archive notes
- Backup notes to Google Drive
- App Shortcuts (on Android 7.1+)
- Optional Light Theme

## Instructions for building
This app uses the Google Drive API to backup and restore databases. The Drive API requires you to authorize your app with OAuth 2.0
 credentials based on your application's signing keys. Enable the Drive API in the [Google Developer Console](https://console.developers.google.com/apis/) and generate these credentials by providing the SHA-1 for your signing key.

NOTE: You will need to edit the signing configs in the app level build.gradle

## Licence
     Copyright © 2016-2017  Samriddha Basu
     This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
     This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
        See http://www.gnu.org/licenses/ for more info
