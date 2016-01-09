
package info.freelibrary.xquery.marc;

import static org.marc4j.Constants.MARCXML_NS_PREFIX;
import static org.marc4j.Constants.MARCXML_NS_URI;
import static org.marc4j.MarcXmlWriter.COLLECTION;
import static org.marc4j.MarcXmlWriter.RECORD;

import org.basex.query.value.node.FElem;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class BaseXContentHandler extends DefaultHandler implements ContentHandler {

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