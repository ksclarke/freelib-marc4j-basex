xquery version "3.0";

declare namespace marcxml="http://www.loc.gov/MARC21/slim";

import module namespace marc="http://freelibrary.info/xquery/marc";
import module namespace db = "http://basex.org/modules/db";

declare function local:error($err-code, $err-message, $err-value) {
  if (exists($err-value))
  then '[' || $err-code || '] ' || $err-message || ": '" || $err-value || "'"
  else '[' || $err-code || '] ' || $err-message
};

((: Running a series of tests: some that are supposed to pass and others that are supposed to fail :)
try {
  let $marc := marc:read('src/test/resources/collection.mrc')
  let $result := count($marc//marcxml:leader) eq 2
  return
    if ($result)
    then db:output('[INFO] Successfully read in a MARC file containing two MARC records')
    else db:output('[ERROR] Failed to successfully read in a MARC file containing two MARC records')
} catch * {
  db:output('[ERROR] Unexpected error thrown while reading MARC: ' || local:error($err:code, $err:description, $err:value))
},
try {
  let $marc := marc:read('src/test/resources/bad_leaders_10_11.mrc')
  let $result := count($marc//marcxml:leader) eq 2
  return
    if ($result)
    then db:output('[ERROR] Successfully read a bad MARC record when it should have thrown an exception')
    else db:output('[ERROR] Failed to successfully read in a MARC record, but did not throw an exception like it should have')
} catch * {
  db:output('[INFO] Successfully threw an exception: ' || local:error($err:code, $err:description, $err:value))
},
try {
  (: Storing records in BaseX needs to be done from a different XQuery than reads them; we store here, read elsewhere :)
  marc:store('src/test/resources/collection.mrc', 'db')
} catch * {
  db:output('[ERROR] Trying to store records threw an exception: ' || local:error($err:code, $err:description, $err:value))
}
)
