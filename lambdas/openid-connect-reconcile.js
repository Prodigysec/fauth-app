/**
* This is the default OpenID Connect reconcile, modify this to your liking. Modify the user
* and registration objects using the incoming values from the identity provider.
*
* @param {Object} user - the FusionAuth user
* @param {Object} registration - the FusionAuth user registration
* @param {Object} jwt - the JSON response from the UserInfo endpoint
* @param {Object} [idToken] - the decoded JSON payload of the id_token. The id_token is available when the
*                             'openid' scope was requested, and the signature can be successfully verified.
* @param {Object} tokens an object containing the encoded access_token and when available the id_token. The
*                        id_token is only available when returned by the IdP and the signature has been verified.
*/
function reconcile(user, registration, jwt, idToken, tokens) {
  // Un-comment this line to see the jwt object printed to the event log
  // console.info(JSON.stringify(jwt, null, 2));

  user.firstName = jwt.given_name;
  user.middleName = jwt.middle_name;
  user.lastName = jwt.family_name;
  user.fullName = jwt.name;
  user.imageUrl = jwt.picture;
  user.mobilePhone = jwt.phone_number;

  // https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
  if (jwt.birthdate && jwt.birthdate !== '0000') {
    if (jwt.birthdate.length === 4) {
      // Only a year was provided, set to January 1.
      user.birthDate = jwt.birthdate + '-01-01';
    } else {
      user.birthDate = jwt.birthdate;
    }
  }

  // https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
  if (jwt.locale) {
    user.preferredLanguages = user.preferredLanguages || [];
    // Replace the dash with an under_score.
    user.preferredLanguages.push(jwt.locale.replace('-', '_'));
  }

  // Set preferred_username in registration.
  // - This is just for display purposes, this value cannot be used to uniquely identify
  //   the user in FusionAuth.
  registration.username = jwt.preferred_username;
}
