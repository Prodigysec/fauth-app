/**
* This is the default Google reconcile, modify this to your liking. Modify the user
* and registration objects using the incoming values from the identity provider.
*
* @param {Object} user - the FusionAuth user
* @param {Object} registration - the FusionAuth user registration
* @param {Object} idToken - the decoded JSON payload returned by the Google Token Info API.
*/
function reconcile(user, registration, idToken) {
  // Un-comment this line to see the idToken object printed to the event log
  // console.info(JSON.stringify(idToken, null, 2));

  // The idToken is the response from the tokeninfo endpoint
  // https://developers.google.com/identity/sign-in/web/backend-auth#calling-the-tokeninfo-endpoint
  user.firstName = idToken.given_name;
  user.lastName = idToken.family_name;
  user.fullName = idToken.name;
  user.imageUrl = idToken.picture;
}
