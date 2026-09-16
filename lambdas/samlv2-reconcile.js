/**
* This is the default SAML v2 reconcile, modify this to your liking. Modify the user
* and registration objects using the incoming values from the identity provider.
*
* @param {Object} user - the FusionAuth user
* @param {Object} registration - the FusionAuth user registration
* @param {Object} samlResponse - the SAML AuthN response returned by the SAML v2 Identity Provider. This
*                                object is a parsed version of the actual XML document returned. See
*                                the FusionAuth documentation for more details, or print the contents of this
*                                object to the event log to assist in debugging.
*/
function reconcile(user, registration, samlResponse) {
  // Un-comment this line to see the samlResponse object printed to the event log
  // console.info(JSON.stringify(samlResponse, null, 2));

  var getAttribute = function(samlResponse, attribute) {
    var values = samlResponse.assertion.attributes[attribute];
    if (values && values.length > 0) {
      return values[0];
    }

    return null;
  };

  // Retrieve an attribute from the samlResponse
  // - Arguments [2 .. ] provide a preferred order of attribute names to lookup the value in the response.
  var defaultIfNull = function(samlResponse) {
    for (var i = 1; i < arguments.length; i++) {
      var value = getAttribute(samlResponse, arguments[i]);
      if (value !== null) {
        return value;
      }
    }
  };

  user.birthDate = defaultIfNull(samlResponse, 'http://schemas.xmlsoap.org/ws/2005/05/identity/claims/dateofbirth', 'birthdate', 'date_of_birth');
  user.firstName = defaultIfNull(samlResponse, 'http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname', 'first_name');
  user.lastName = defaultIfNull(samlResponse, 'http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname', 'last_name');
  user.fullName = defaultIfNull(samlResponse, 'http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name', 'name', 'full_name');
  user.mobilePhone = defaultIfNull(samlResponse, 'http://schemas.xmlsoap.org/ws/2005/05/identity/claims/mobilephone', 'mobile_phone');
}
