xquery version "3.1";

declare namespace marcxml = "http://www.loc.gov/MARC21/slim";

declare function local:error($err-code, $err-message, $err-value) {
  if (exists($err-value))
  then '[' || $err-code || '] ' || $err-message || ": '" || $err-value || "'"
  else '[' || $err-code || '] ' || $err-message
};

try {
  for $thing in collection('db')
  return $thing
  (:
    if ($result and (count($leaders) = 2))
    then '[INFO] Successfully read in two MARC records and stored then as MARCXML in a new collection'
    else '[ERROR] Failed to successfully store two MARC records as MARCXML in a new collection'
  :)
} catch * {
  '[ERROR] Trying to store records threw an exception: ' || local:error($err:code, $err:description, $err:value)
}