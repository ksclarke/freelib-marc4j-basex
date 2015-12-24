
package info.freelibrary.xquery.marc;

import static org.marc4j.Constants.MARCXML_NS_PREFIX;
import static org.marc4j.Constants.MARCXML_NS_URI;
import static org.marc4j.MarcXmlWriter.COLLECTION;
import static org.marc4j.MarcXmlWriter.RECORD;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;

import javax.xml.transform.sax.SAXResult;

import org.basex.core.Context;
import org.basex.io.serial.SAXSerializer;
import org.basex.query.QueryException;
import org.basex.query.QueryModule;
import org.basex.query.QueryResource;
import org.basex.query.value.Value;
import org.basex.query.value.ValueBuilder;
import org.basex.query.value.item.Item;
import org.basex.query.value.node.FElem;
import org.basex.query.value.type.Type;
import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlHandler;
import org.marc4j.MarcXmlWriter;
import org.marc4j.RecordStack;
import org.marc4j.marc.Record;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import info.freelibrary.marc4j.converter.impl.AnselToUnicode;
import info.freelibrary.marc4j.converter.impl.UnicodeToAnsel;

/**
 * An extension module for reading and writing MARC (MAchine Readable Cataloging) records from and to the file system.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class BaseXModule extends QueryModule implements QueryResource {

    /**
     * Writes MARCXML records to a MARC file.
     *
     * @return True if the records were successfully written
     */
    public boolean write(final Value aRecSeq, final String aFilePath) throws QueryException {
        MarcWriter writer = null;

        try {
            final Iterator<Item> iterator = aRecSeq.iterator();
            final RecordStack recordStack = new RecordStack();
            final MarcXmlHandler marcHandler = new MarcXmlHandler(recordStack);

            writer = new MarcStreamWriter(new FileOutputStream(aFilePath));
            writer.setConverter(new UnicodeToAnsel());

            while (iterator.hasNext()) {
                final Item item = iterator.next();

                if (item.typeId().equals(Type.ID.ELM)) {
                    final SAXSerializer serializer = new SAXSerializer(item);

                    serializer.setContentHandler(marcHandler);
                    serializer.parse("");

                    if (recordStack.hasNext()) {
                        final Record record = recordStack.pop();

                        try {
                            writer.write(record);
                        } catch (final MarcException details) {
                            String recordId = record.getControlNumber();

                            if (recordId != null) {
                                recordId = recordId.trim();
                            } else {
                                recordId = "UNKNOWN RECORD ID";
                            }

                            throw new QueryException("Record [" + recordId + "]: " + details.getMessage());
                        } finally {
                            try {
                                serializer.close();
                            } catch (final IOException details) {
                                throw new QueryException(details.getMessage());
                            }
                        }
                    } else {
                        try {
                            serializer.close();
                        } catch (final IOException details) {
                            throw new QueryException("Didn't read MARC record and couldn't close serializer");
                        }

                        throw new QueryException("Didn't read MARC record");
                    }
                } else {
                    throw new QueryException("Write takes record element or a sequence of record elements");
                }

                writer.close();
            }
        } catch (final FileNotFoundException details) {
            throw new QueryException(details.getMessage());
        } catch (final SAXException details) {
            throw new QueryException(details.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        return true;
    }

    /**
     * Stores MARC records in a database collection as MARCXML.
     *
     * @param aFilePath
     * @param aCollection
     * @return
     */
    public String store(final String aFilePath, final String aDbPath) throws QueryException {
        final String dbPath = aDbPath.startsWith("/") ? aDbPath.substring(1) : aDbPath;
        final String db = dbPath.split("\\/")[0];
        final StringBuilder sb = new StringBuilder();

        final Context context = new Context();

        sb.append("[to be implemented]");
        context.close();

        return sb.toString();
    }

    /**
     * Reads MARC records from a file into memory as MARCXML records.
     *
     * @return A sequence of MARC records
     */
    public Value read(final String aFilePath) throws QueryException {
        final BaseXContentHandler handler = new BaseXContentHandler();
        final SAXResult result = new SAXResult(handler);
        final MarcWriter writer = new MarcXmlWriter(result);
        final ValueBuilder vb = new ValueBuilder();

        InputStream inputStream = null;
        MarcReader reader;

        writer.setConverter(new AnselToUnicode());

        try {
            inputStream = new FileInputStream(getFile(aFilePath));
            reader = new MarcStreamReader(inputStream);

            while (reader.hasNext()) {
                writer.write(reader.next());
                vb.add(handler.getRecord());
            }

            writer.close();

            return vb.value();
        } catch (final MarcException details) {
            throw new QueryException(details.getMessage());
        } catch (final IOException details) {
            throw new QueryException(details.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException details) {
                    throw new QueryException(details.getMessage());
                }
            }
        }
    }

    @Override
    public void close() {

    }

    private File getFile(final String aFileId) throws QueryException {
        File baseDir;

        if (aFileId.startsWith("file:")) {
            try {
                baseDir = new File(new URI(aFileId));
            } catch (final Exception details) {
                throw new QueryException(details.getMessage());
            }
        } else {
            baseDir = new File(aFileId);
        }

        return baseDir;
    }

    private class BaseXContentHandler extends DefaultHandler implements ContentHandler {

        private FElem myElement;

        private StringBuilder myText;

        @Override
        public void characters(final char[] aChars, final int aStart, final int aLength) throws SAXException {
            myText.append(new String(aChars, aStart, aLength));
        }

        @Override
        public void endElement(final String aURI, final String aLocalName, final String aQName) throws SAXException {
            if (myText.length() > 0) {
                myElement.add(myText.toString());
                myText.delete(0, myText.length());
            }

            if (!aLocalName.equals(COLLECTION) && !aLocalName.equals(RECORD)) {
                myElement = (FElem) myElement.parent();
            }
        }

        @Override
        public void startDocument() {
            myText = new StringBuilder();
        }

        @Override
        public void startElement(final String aURI, final String aLocalName, final String aQName,
                final Attributes aAttributes) throws SAXException {
            if (aLocalName.equals(RECORD)) {
                myElement = new FElem(MARCXML_NS_PREFIX, RECORD, MARCXML_NS_URI);

                for (int index = 0; index < aAttributes.getLength(); index++) {
                    myElement.add(aAttributes.getLocalName(index), aAttributes.getValue(index));
                }
            } else if (!aLocalName.equals(COLLECTION)) {
                final FElem element = new FElem(MARCXML_NS_PREFIX, aLocalName, MARCXML_NS_URI);

                for (int index = 0; index < aAttributes.getLength(); index++) {
                    element.add(aAttributes.getLocalName(index), aAttributes.getValue(index));
                }

                myElement.add(element);
                myElement = element;
            }
        }

        public FElem getRecord() {
            return getRoot(myElement);
        }

        /**
         * Return the root of the supplied element.
         *
         * @param aElement A parent from which to find the root
         * @return The root element
         */
        private FElem getRoot(final FElem aElement) {
            final FElem parent = (FElem) aElement.parent();

            if (parent != null) {
                return getRoot(parent);
            } else {
                return aElement;
            }
        }
    }
}
