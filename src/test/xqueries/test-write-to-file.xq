xquery version "3.0";

declare namespace marcxml = "http://www.loc.gov/MARC21/slim";

import module namespace marc = "http://freelibrary.info/xquery/marc";
import module namespace fetch = "http://basex.org/modules/fetch";
import module namespace file = "http://expath.org/ns/file";

declare function local:get-timestamp() {
  (current-dateTime() - xs:dateTime("1970-01-01T00:00:00-00:00")) div xs:dayTimeDuration('PT0.001S')
};

declare function local:error($err-code, $err-message, $err-value) {
  if (exists($err-value))
  then '[' || $err-code || '] ' || $err-message || ": '" || $err-value || "'"
  else '[' || $err-code || '] ' || $err-message
};

((: Running a series of tests: some that are supposed to pass and others that are supposed to fail :)
try {
  (: First, let's try parsing a single MARCXML record :)
  let $record := fetch:xml('src/test/resources/record.marc.xml')/*
  let $result := marc:write($record,  file:temp-dir() || '/record-' || local:get-timestamp() || '.mrc')
  let $result :=
    if ($result)
    then
      (: Next, let's try parsing a collection of MARCXML records; to do this, we need to pass in the record elements :)
      let $records := fetch:xml('src/test/resources/collection.marc.xml')//marcxml:record
      let $result := marc:write($records, file:temp-dir() || '/collection-' || local:get-timestamp() || '.mrc')
      return $result
    else false()
  return
    if ($result)
    then '[INFO] Successfully ran the tests that check the ability to write valid MARC'
    else '[ERROR] Failed to successfully complete the tests that check the ability to write valid MARC: Empty MARC sequence'
} catch * {
  '[ERROR] Caught an exception where there should not have been one: '  || local:error($err:code, $err:description, $err:value) 
},
try {
  (: Let's try parsing an XML file that is not valid MARC :)
  let $record := fetch:xml('src/test/resources/badrecord.marc.xml')/*
  let $timestamp := local:get-timestamp()
  let $result := marc:write($record, file:temp-dir() || '/badrecord-' || $timestamp || '.mrc')
  return
    if ($result)
    then (
      '[ERROR] Failed to throw an exception when trying to write an invalid MARC record',
      marc:read(file:temp-dir() || '/badrecord-' || $timestamp || '.mrc')
    )
    else '[ERROR] Did not write invalid MARC, but also did not throw an exception like expected'
} catch * {
  '[INFO] Successfully threw an exception: ' || local:error($err:code, $err:description, $err:value)
},
try {
  let $result := marc:write(<not-a-record/>, file:temp-dir() || '/not-record' || local:get-timestamp() || '.mrc')
  return
    if ($result)
    then '[ERROR] Failed to throw an exception when trying to write an invalid MARC record'
    else '[ERROR] Did not write invalid MARC, but also did not throw an exception like expected'
} catch * {
  '[INFO] Successfully threw an exception: ' || local:error($err:code, $err:description, $err:value) 
}
)
