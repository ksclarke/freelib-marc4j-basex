
package info.freelibrary.xquery.marc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.sax.SAXResult;

import org.basex.query.QueryException;
import org.basex.query.QueryModule;
import org.basex.query.QueryResource;
import org.basex.query.value.Value;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;

import info.freelibrary.marc4j.converter.impl.AnselToUnicode;

/**
 * An extension module for reading MARC (MAchine Readable Cataloging) records from the file system into the database.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class BaseXIterModule extends QueryModule implements QueryResource {

    private InputStream myInputStream;

    private MarcReader myReader;

    private MarcWriter myWriter;

    private BaseXContentHandler myHandler;

    /**
     * Opens a MARC file for reading.
     *
     * @param aFilePath A path to the MARC file
     * @throws QueryException If there is problem reading from the MARC file
     */
    public void open(final String aFilePath) throws QueryException {
        myHandler = new BaseXContentHandler();
        myWriter = new MarcXmlWriter(new SAXResult(myHandler));
        myWriter.setConverter(new AnselToUnicode());

        try {
            myInputStream = new FileInputStream(BaseXModuleUtils.getFile(aFilePath));
            myReader = new MarcStreamReader(myInputStream);
        } catch (final IOException details) {
            throw new QueryException(details.getMessage());
        }
    }

    /**
     * Returns true if there is another MARC record to read.
     *
     * @return True if there is another MARC record to read
     */
    public boolean hasNext() {
        return myReader != null && myReader.hasNext();
    }

    /**
     * Returns the next MARC record from the file as a MARCXML record.
     *
     * @return The next MARC record from the file as a MARCXML record
     */
    public Value next() {
        myWriter.write(myReader.next());
        return myHandler.getRecord();
    }

    @Override
    public void close() {
        if (myInputStream != null) {
            try {
                myInputStream.close();
            } catch (final IOException details) {
                System.err.println(details.getMessage());
            }
        }

        if (myWriter != null) {
            myWriter.close();
        }
    }

}
