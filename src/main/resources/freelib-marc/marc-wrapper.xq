module namespace marc = "http://freelibrary.info/xquery/marc";

import module namespace marc4j = "java:info.freelibrary.xquery.marc.BaseXModule";

(: Read a MARC file into memory as MARCXML :)
declare function marc:read($file as xs:string) as element()* {
  marc4j:read($file)
};

(: Read a MARC file into a database collection as MARCXML :)
declare function marc:store($file as xs:string, $dbPath as xs:string) {
  marc4j:store($file, $dbPath)
};

(: Write a sequence of MARCXML records to a MARC file :)
declare function marc:write($record as element()*, $file as xs:string) as xs:boolean {
  marc4j:write($record, $file)
};