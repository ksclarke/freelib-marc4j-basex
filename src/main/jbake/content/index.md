title=Welcome to the FreeLib-MARC4J-BaseX Project
date=2015-12-23
type=page
status=published
~~~~~~

FreeLib-MARC4J-BaseX is an extension library for [BaseX](http://basex.org/) (a native XML database). It allows the reading and writing of MARC records into and out from the database. It's packaged as a XAR file (which can be used to install the freelib-marc4j-basex code into an BaseX database).  You can choose to build the XAR file yourself or install a previously built file from the project's GitHub [releases](https://github.com/ksclarke/freelib-marc4j-basex/releases) page.

Once you've built (or downloaded a pre-built version of) the XAR file and installed it, check out some examples of how to use FreeLib-MARC4J-BaseX on the [Introduction](introduction.html) page.

<script>
xmlhttp=new XMLHttpRequest();
xmlhttp.open("GET", "http://freelibrary.info/mvnlookup.php?project=freelib-marc4j-basex", false);
xmlhttp.send();
$version = xmlhttp.responseText;
</script>

## Building the Project

The project is a Java project and is built using Maven.  You will need a [JDK](http://openjdk.java.net/) (version 8 or greater) and [Maven](https://maven.apache.org/) (version 3 or greater) installed before you'll be able to build the project.  To build the project, check it out from GitHub and run Maven from within the project directory:

    git clone https://github.com/ksclarke/freelib-marc4j-basex.git
    cd freelib-marc4j-basex
    mvn install

<br/>After that, you should be able to find the newly built XAR file -- <code>freelib-marc4j-basex-<script>document.write($version);</script><noscript>${version}</noscript>.xar</code> -- in the project's <code>target</code> directory.

## Installing the XAR

You can install the freelib-marc4j-basex XAR file from the command line.

    java -cp basex-8.3.1.jar org.basex.BaseX -cREPO INSTALL file:///path/to/freelib-marc4j-basex-<script>document.write($version);</script><noscript>${version}</noscript>.xar

Now, you are ready to read and write some MARC records!

## Potential Gotchas

<br/>There are not any known gotchas at this time, but since this is the first version of this library I'm sure there are some bugs (or at the very least some things that could be done better).  Please feel free to share your experiences using the library so that I can make it better.

## License

<br/>[GNU Lesser General Public License, Version 3.0 (or any later version)](LICENSE.txt)

## Contact

<br/>If you have questions about [freelib-marc4j-basex](http://github.com/ksclarke/freelib-marc4j-basex) feel free to ask them on the FreeLibrary Projects [mailing list](https://groups.google.com/forum/#!forum/freelibrary-projects); or, if you encounter a problem, please feel free to [open an issue](https://github.com/ksclarke/freelib-marc4j-basex/issues "GitHub Issue Queue") in the project's issue queue.
