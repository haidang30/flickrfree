### 1.2.0 ###

  * Fixed authentication bug.
  * Removed automatic pop-up of upgrade dialog at initial startup.

### 1.1.9 ###

  * Fixed FC when receiving bad data from server.

### 1.1.8 ###

  * Fixed FC when searching and commenting.
  * Fixed "invalid signature" error when searching.

### 1.1.7 ###

  * Fixed FC when opening Options Menu for some images.

### 1.1.6.1 ###

  * Fixed a FC that was happening for some people at startup.

### 1.1.6 ###

  * Full-screen image menu changed from Context Menu to Options Menu.
  * New icon (thanks to Aaron Delani!)
  * Link provided to upgrade to Flickr Companion.

### 1.1.5 ###

  * Fixed authentication screen to make the authentication procedure a little more clear and avoid confusion between the Flickr login itself and the authentication code used by the app.

### 1.1.4 ###

  * Added a progress bar to the Authentication screen when loading authentication web page.

  * Fixed a potential problem in the Authentication URL.

### 1.1.3 ###

  * Fixed a FC bug in authentication screen.

### 1.1.2 ###

  * Improved authentication screen. It is now easier to set up your account(s).

### 1.1.1 ###

  * Fixed a memory bug that can cause a FC after downloading large pictures on some (mainly older) phones.

### 1.1 ###

  * Big improvements to downloader. Downloads now happen in the background (i.e., no UI hang when you start a download), and there is a progress-bar notification for downloads just like there is for uploads. It can even upload and download simultaneously.

### 1.0.3 ###

  * Fixes for "stuck" upload notification. Sometimes the Uploader service doesn't get the broadcast notification telling it that an upload is complete, so it doesn't know to clear the notification. Now if the user taps on the notification, taking them to the UploadProgress screen, and all uploads are finished, it will know to clear the notification.

### 1.0.2 ###

  * Fixed some bugs in the Uploader.

### 1.0.1 ###

  * Fixed a NullPointerException that can cause FCs in the Collections view.

### 1.0 ###

  * Better timeout handling on uploads. If there is a connection failure, it will fail gracefully and inform the user with a Toast notification. The status-bar notification won't hang around forever if the upload fails either.

### 0.9.9.9.1 ###

  * A few bug fixes.

### 0.9.9.9 ###

  * Improved display of Comments.

### 0.9.9.8 ###

  * Added intent filter for ACTION\_SEND, allowing photos to be uploaded directly from the "Share" button in the Media gallery.

### 0.9.9.7.3 ###

  * Improved layout of Picture Settings (formerly known as Upload Options) screen to better fit with minimal scrolling.

  * Added progress bar to upload status bar notification expanded view.

### 0.9.9.7.2 ###

  * Fixed bug that causes Force Close if user selects "Upload", then backs out of Media Gallery without picking an image.

### 0.9.9.7.1 ###

  * Fixed bug that causes Force Close if user selects "Groups" before group information has finished loading.

### 0.9.9.7 ###

  * Upload progress screen now uses a progress bar instead of just a percentage display to show progress of current download.

  * Upload progress screen closes automatically when the last upload finishes.

### 0.9.9.6 ###

  * Upload queue, allowing user to start a new upload before the current one is finished.

  * Upload Progress screen, which displays the upload queue and gives percentage complete for the current upload.

### 0.9.9.5 ###

  * Fixed a bug in retrieving groups.

### 0.9.9.4 ###

  * Made uploader status-bar notification persistent.

### 0.9.9.3 ###

  * Added status-bar notification to uploader.

### 0.9.9.2 ###

  * Fixed another serious authentication bug for new users.

### 0.9.9.1 ###

  * Fixed a bug that prevents new users from setting up their accounts.

### 0.9.9 ###

  * Added uploading capability.

### 0.9.8.3 ###

  * Now has the ability to upload photos.

### 0.9.8.3 ###

  * Moved cache from SD card back to default (internal memory) cache location.

### 0.9.8.2 ###

  * Fixed a bug that crashes the app if the user tries to open the Comments page on a picture that has no comments.

### 0.9.8.1 ###

  * Fixed a bug in 0.9.8 that prevents user from viewing other users' pages.

### 0.9.8 ###

  * Better interface for adding, switching, and removing accounts.

  * Fixed annoying bug that can cause authentication to be lost if account force-closes or is idle for a long time.

### 0.9.7.2 ###

  * Added some code to provide more useful information and some help if user authentication fails.

### 0.9.7.1 ###

  * Code cleanup

  * Added a more informative error dialog if the user tries to open a group that is not visible to them.

  * Added buddy icon to button under username in Image Info screen.

### 0.9.7 ###

  * Users can now look at their groups and other users' (public) groups.

### 0.9.6 ###

  * Users can now comment on photos.

  * Photos with large number of comments are now handled better.

### 0.9.5 ###

  * Improved Image Context view.

  * Added clickable links to image comments. If a comment has URLs referring to other groups, just tap the comment and it will bring up a list of those groups. Tap on one of them to be taken to that group inside the app.

### 0.9.4.2 ###

  * Fixed handling of HTML markup in comment text.

### 0.9.4.1 ###

  * Fixed a bug that can cause an exception on creation of UserView if RestClient is not able to return a valid JSONObject.

### 0.9.4 ###

  * Fixed (hopefully for the last time) authentication bugs. Current users might need to re-authenticate one more time to clear things out. Apologies for the inconvenience.

### 0.9.3 ###

  * Added ability to view photo comments through the long-press menu in Picture View screen.

### 0.9.2 ###

  * Fixed stupid authentication bug that prevented it from linking to the new API key.

### 0.9.1 ###

  * Changed to numeric keypad for entering authentication code.

  * Had to switch to a different API key for this version. Current users will have to re-authenticate. Apologies.