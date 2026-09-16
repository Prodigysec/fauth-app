/**
* This is the default LinkedIn reconcile, modify this to your liking. Modify the user
* and registration objects using the incoming values from the identity provider.
*
* @param {Object} user - the FusionAuth user
* @param {Object} registration - the FusionAuth user registration
* @param {Object} linkedInUser - the decoded JSON payload returned by the LinkedIn Me API or the LinkedIn UserInfo API
*/
function reconcile(user, registration, linkedInUser) {
  // Un-comment this line to see the linkedInUser object printed to the event log
  // console.info(JSON.stringify(linkedInUser, null, ' '));

  // Depending on how and when you have set up your LinkedIn application you may get a different response back in the linkedInUser.
  //
  // The first checks apply if you are using the "openid", "email", and "profile" scopes.
  // If so FusionAuth will call the LinkedIn UserInfo API.
  // See https://learn.microsoft.com/en-us/linkedin/consumer/integrations/self-serve/sign-in-with-linkedin-v2#api-request-to-retreive-member-details
  //
  // The second checks apply if you are using the legacy program and Profile API with the "r_liteprofile" or "r_basicprofile" scopes.
  // See https://learn.microsoft.com/en-us/linkedin/shared/integrations/people/profile-api

  if (linkedInUser.given_name) {
    user.firstName = linkedInUser.given_name;
  } else if (linkedInUser.localizedFirstName) {
    user.firstName = linkedInUser.localizedFirstName;
  }

  if (linkedInUser.family_name) {
    user.lastName = linkedInUser.family_name;
  } else if (linkedInUser.localizedLastName) {
    user.lastName = linkedInUser.localizedLastName;
  }

  if (linkedInUser.picture) {
    // UserInfo will only supply one image size
    user.imageUrl = linkedInUser.picture;
  } else if (linkedInUser.profilePicture){
    // LinkedIn may return several images sizes.
    // See https://docs.microsoft.com/en-us/linkedin/shared/references/v2/profile/profile-picture
    // We'll sort the array by descending size and then grab the largest one.
    var images = linkedInUser.profilePicture['displayImage~'].elements || [];
    images.sort(function(a, b) {
      return b.data["com.linkedin.digitalmedia.mediaartifact.StillImage"].displaySize.width - a.data["com.linkedin.digitalmedia.mediaartifact.StillImage"].displaySize.width;
    });
    if (images.length > 0) {
      user.imageUrl = images[0].identifiers[0].identifier;
    }
  }
}
