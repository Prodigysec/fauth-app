/**
* This is the default Apple reconcile, modify this to your liking. Modify the user
* and registration objects using the incoming values from the identity provider.
*
* @param {Object} user - the FusionAuth user
* @param {Object} registration - the FusionAuth user registration
* @param {Object} idToken - the decoded JSON payload of the id_token returned by Apple.
*/
function reconcile(user, registration, idToken) {
  // Un-comment this line to see the idToken object printed to the event log
  // console.info(JSON.stringify(idToken, null, 2));

  // During the first login attempt, the user object will be available which may contain first and last name.
  if (idToken.user && idToken.user.name) {
    user.firstName = idToken.user.name.firstName || user.firstName;
    user.lastName = idToken.user.name.lastName || user.lastName;
  }
}
