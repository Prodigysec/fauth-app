// This is the default SCIM Group request converter, modify this to your liking.
function convert(group, members, options, scimGroup) {

  // Un-comment this line to see the scimGroup object printed to the event log
  // console.info(JSON.stringify(scimGroup, null, 2));

  // Request options
  // FusionAuth allows you to assign one or more application roles to a group.
  // To use this feature, assign one or more application Ids here.
  // options.roleIds = [];

  // Set the name of the group using the SCIM Group displayName
  // Note, the group.name field is required.
  group.name = scimGroup.displayName;

  // Build a members array with a userId and a $ref in custom data
  if (scimGroup.members) {
    for (var i = 0; i < scimGroup.members.length; i++) {
      members.push({
        userId: scimGroup.members[i].value,
        data: {
          $ref: scimGroup.members[i]['$ref']
        }
      });
    }
  }
}
