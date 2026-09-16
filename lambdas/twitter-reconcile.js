/**
* This is the default Twitter reconcile, modify this to your liking. Modify the user
* and registration objects using the incoming values from the identity provider.
*
* @param {Object} user - the FusionAuth user
* @param {Object} registration - the FusionAuth user registration
* @param {Object} twitterUser - the decoded JSON payload returned by the Twitter Verify Credentials API.
*/
function reconcile(user, registration, twitterUser) {
  // Un-comment this line to see the twitterUser object printed to the event log
  // console.info(JSON.stringify(twitterUser, null, 2));

  // Set name if available in the response
  if (twitterUser.name) {
    user.fullName = twitterUser.name;
  }

  // https://developer.twitter.com/en/docs/accounts-and-users/user-profile-images-and-banners.html
  if (twitterUser.profile_image_url_https) {
    // Remove the _normal suffix to get the original size.
    user.imageUrl = twitterUser.profile_image_url_https.replace('_normal.png', '.png');
  }

  // Set twitter screen_name in registration.
  // - This is just for display purposes, this value cannot be used to uniquely identify
  //   the user in FusionAuth.
  registration.username = twitterUser.screen_name;
}
