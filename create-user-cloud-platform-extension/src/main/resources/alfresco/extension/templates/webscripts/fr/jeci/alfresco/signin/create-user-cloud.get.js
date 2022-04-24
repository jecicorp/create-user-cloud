function main() {
  var groups = people.getContainerGroups(person);
  
  model.organization = person.properties["cm:organization"];
  model.isMember = isMember("GROUP_CREATE_USER_ADMINISTRATORS", groups);
}

function isMember(groupName, groups) {
  for (var i = 0 ; i < groups.length ; i++) {
    if (groupName == groups[i].properties["cm:authorityName"]) {
      return true;
    }
  }
  return false;
}

main();