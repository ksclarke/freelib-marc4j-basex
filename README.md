# FreeLib-MARC4J-BaseX [![Build Status](https://travis-ci.org/ksclarke/freelib-marc4j-basex.png?branch=master)](https://travis-ci.org/ksclarke/freelib-marc4j-basex)

This is an extension for [BaseX](http://basex.org/) (a native XML database) that allows the reading and writing of MARC records into and out from the database. The product of the project's build is an XAR file which can be used to install the freelib-marc4j-basex code into a BaseX database.  You can either build the project yourself or install a previously built XAR file from the project's GitHub [releases](https://github.com/ksclarke/freelib-marc4j-basex/releases) page.

## Getting Started

Once you have installed the project's XAR file in your BaseX database, you should be able to run some simple XQueries that will read in MARC as MARCXML.  For instance:

    xquery version "3.0";
    
    import module namespace marc="http://freelibrary.info/xquery/marc";
    
    (: Read the MARC records into memory as MARCXML records :)
    let $marc := marc:read('/path/to/marc-records.mrc')
    
    (: Then write them with xmldb functions or do something else with them :)
    return $marc

If you have more records than will fit into memory, you may want to write them directly into a collection in the database, where you can do further work on them (moving them into other collections or munging their data in place).  For instance:

    xquery version "3.0";
    
    declare namespace marcxml="http://www.loc.gov/MARC21/slim";
    
    import module namespace marc="http://freelibrary.info/xquery/marc";
    
    (: Read the MARC records into the 'marc-records' collection as MARCXML records :)
    let $result := marc:store('/path/to/marc-records.mrc', '/db/marc-records')
    
    (: You can then query your collection and do things with the stored MARCXML records :)
    let $leaders := collection('/db/marc-records')//marcxml:leader
    return count($leaders)

You can also write out MARCXML records from your BaseX database as MARC records (and then load them into your ILS if you want); for instance:

    xquery version "3.0";
    
    import module namespace marc="http://freelibrary.info/xquery/marc";
    
    let $record :=
      <record xmlns="http://www.loc.gov/MARC21/slim">
        <leader>01142cam  2200301 a 4500</leader>
        <controlfield tag="001">   92005291 </controlfield>
        <controlfield tag="003">DLC</controlfield>
        <controlfield tag="005">19930521155141.9</controlfield>
        <controlfield tag="008">920219s1993    caua   j      000 0 eng  </controlfield>
        <datafield tag="010" ind1=" " ind2=" ">
          <subfield code="a">   92005291 </subfield>
        </datafield>
        <datafield tag="020" ind1=" " ind2=" ">
          <subfield code="a">0152038655 :</subfield>
          <subfield code="c">$15.95</subfield>
        </datafield>
        <datafield tag="040" ind1=" " ind2=" ">
          <subfield code="a">DLC</subfield>
        </datafield>
        <datafield tag="050" ind1="0" ind2="0">
          <subfield code="a">PS3537.A618</subfield>
          <subfield code="b">A88 1993</subfield>
        </datafield>
        <datafield tag="100" ind1="1" ind2=" ">
          <subfield code="a">Sandburg, Carl,</subfield>
          <subfield code="d">1878-1967.</subfield>
        </datafield>
        <datafield tag="245" ind1="1" ind2="0">
          <subfield code="a">Arithmetic /</subfield>
          <subfield code="c">Carl Sandburg ; illustrated as an anamorphic adventure by Ted Rand.</subfield>
        </datafield>
        <datafield tag="250" ind1=" " ind2=" ">
          <subfield code="a">1st ed.</subfield>
        </datafield>
        <datafield tag="260" ind1=" " ind2=" ">
          <subfield code="a">San Diego :</subfield>
          <subfield code="b">Harcourt Brace Jovanovich,</subfield>
          <subfield code="c">c1993.</subfield>
        </datafield>
        <datafield tag="300" ind1=" " ind2=" ">
          <subfield code="a">1 v. (unpaged) :</subfield>
          <subfield code="b">ill. (some col.) ;</subfield>
          <subfield code="c">26 cm.</subfield>
        </datafield>
        <datafield tag="650" ind1=" " ind2="0">
          <subfield code="a">Arithmetic</subfield>
          <subfield code="x">Juvenile poetry.</subfield>
        </datafield>
        <datafield tag="650" ind1=" " ind2="0">
          <subfield code="a">Children's poetry, American.</subfield>
        </datafield>
        <datafield tag="650" ind1=" " ind2="1">
          <subfield code="a">Arithmetic</subfield>
          <subfield code="x">Poetry.</subfield>
        </datafield>
        <datafield tag="650" ind1=" " ind2="1">
          <subfield code="a">American poetry.</subfield>
        </datafield>
        <datafield tag="700" ind1="1" ind2=" ">
          <subfield code="a">Rand, Ted,</subfield>
          <subfield code="e">ill.</subfield>
        </datafield>
      </record>
    return marc:write($record, '/path/to/marc-record.mrc')

If you want to write out more than one record (which you probably will), you'll need to pass a sequence of MARC records into the write function (not a single MARCXML collection).  For instance:

    xquery version "3.0";
    
    declare namespace marcxml="http://www.loc.gov/MARC21/slim";
    
    import module namespace marc="http://freelibrary.info/xquery/marc";
    import module namespace fetch = "http://basex.org/modules/fetch";
    
    (: First, read in some MARCXML records from the file system :)
    let $records := fetch:xml('src/test/resources/record.marc.xml')//marcxml:record
    
    (: Then write them out as MARC records :)
    return marc:write($records, '/path/to/marc-record.mrc')

If you are interested in seeing some more examples, take a look at the tests in the [src/test/xqueries](https://github.com/ksclarke/freelib-marc4j-basex/tree/master/src/test/xqueries) folder.  There are multiple tests (i.e., examples) in each XQuery file.

## Building the Project

The project is a Java project and is built using [Maven](https://maven.apache.org/).  You will need a JDK (version 8 or greater) and Maven (version 3 or greater) installed before you'll be able to build the project.  To build the project, check it out from GitHub and run Maven from within the project directory:

    git clone https://github.com/ksclarke/freelib-marc4j-basex.git
    cd freelib-marc4j-basex
    mvn install

After you do that you should be able to find the XAR file in the project's `target` directory.  It will be named something like `freelib-marc4j-basex-${VERSION}.xar`.

## Installing the XAR

You can install the freelib-marc4j-basex XAR file from the command line.

    java -cp basex-8.3.1.jar org.basex.BaseX -cREPO INSTALL file:///path/to/freelib-marc4j-basex-0.0.1.xar

Now, you are ready to read and write some MARC records!

## Potential Gotchas

There are not any known gotchas at this time, but since this is the first version of this library I'm sure there are some bugs (or at the very least some things that could be done better).  Please feel free to share your experiences using the library so that I can make it better.

## License

[GNU Lesser General Public License, Version 3.0 (or any later version)](LICENSE.txt)

## Contact

If you have questions about [freelib-marc4j-basex](http://github.com/ksclarke/freelib-marc4j-basex) feel free to ask them on the FreeLibrary Projects [mailing list](https://groups.google.com/forum/#!forum/freelibrary-projects); or, if you encounter a problem, please feel free to [open an issue](https://github.com/ksclarke/freelib-marc4j-basex/issues "GitHub Issue Queue") in the project's issue queue.
