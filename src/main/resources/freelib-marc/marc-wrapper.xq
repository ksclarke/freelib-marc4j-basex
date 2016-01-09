module namespace marc = "http://freelibrary.info/xquery/marc";

declare namespace marcxml = "http://www.loc.gov/MARC21/slim";
declare namespace file = "http://expath.org/ns/file";

import module namespace marc4jMem = "java:info.freelibrary.xquery.marc.BaseXMemModule";
import module namespace marc4jIter = "java:info.freelibrary.xquery.marc.BaseXIterModule";
import module namespace db = "http://basex.org/modules/db";

declare %private %updating function marc:storeRecords($dbPath as xs:string) {
  if (marc4jIter:hasNext() eq true()) then
    let $record := marc4jIter:next()
    let $id := $record/marcxml:controlfield[@tag = '001'] || '.xml'
    return (
(:
      db:output('[DEBUG] ' || count($record/*)),
      db:output($record), :)
      (: db:add($dbPath, '<test><me>asdf</me></test>', 'test-me.xml'), :)
      db:add($dbPath, $record, $id),
      marc:storeRecords($dbPath)
    )
  else ()
};

(: Read a MARC file into memory as MARCXML :)
declare function marc:read($file as xs:string) as element()* {
  marc4jMem:read($file)
};

(: Read a MARC file into a database collection as MARCXML :)
declare %updating function marc:store($file as xs:string, $dbPath as xs:string) {
  let $path := if (starts-with($dbPath, '/')) then substring-after($dbPath, '/') else $dbPath
  let $dbPathTokens := tokenize($path, '/+')
  let $db := $dbPathTokens[1]
  let $collection := subsequence($dbPathTokens, 2)
  return (
    marc4jIter:open($file),
    marc:storeRecords($dbPath)
  )
};

(: Write a sequence of MARCXML records to a MARC file :)
declare function marc:write($record as element()*, $file as xs:string) as xs:boolean {
  marc4jMem:write($record, $file)
};