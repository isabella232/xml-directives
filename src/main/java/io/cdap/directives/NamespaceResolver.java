package io.cdap.directives;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class NamespaceResolver implements NamespaceContext {
  private static final String DEFAULT_NS = "DEFAULT";
  private Map<String, String> prefix2Uri = new HashMap<String, String>();
  private Map<String, String> uri2Prefix = new HashMap<String, String>();

  public NamespaceResolver(Document document, boolean toplevelOnly) {
    String namespaceURI = document.getFirstChild().getNamespaceURI();
    if (namespaceURI != null) {
      putInCache(DEFAULT_NS, namespaceURI);
    }
    examineNode(document.getFirstChild(), toplevelOnly);
  }

  @Override
  public String getNamespaceURI(String prefix) {
    if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
      return prefix2Uri.get(DEFAULT_NS);
    } else {
      return prefix2Uri.get(prefix);
    }
  }

  @Override
  public String getPrefix(String namespaceURI) {
    return uri2Prefix.get(namespaceURI);
  }

  @Override
  public Iterator getPrefixes(String namespaceURI) {
    return null;
  }

  private void examineNode(Node node, boolean attributesOnly) {
    NamedNodeMap attributes = node.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      Node attribute = attributes.item(i);
      storeAttribute((Attr) attribute);
    }

    if (!attributesOnly) {
      NodeList chields = node.getChildNodes();
      for (int i = 0; i < chields.getLength(); i++) {
        Node chield = chields.item(i);
        if (chield.getNodeType() == Node.ELEMENT_NODE)
          examineNode(chield, false);
      }
    }
  }

  private void storeAttribute(Attr attribute) {
    // examine the attributes in namespace xmlns
    if (attribute.getNamespaceURI() != null
      && attribute.getNamespaceURI().equals(
      XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
      // Default namespace xmlns="uri goes here"
      if (attribute.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
        putInCache(DEFAULT_NS, attribute.getNodeValue());
      } else {
        // The defined prefixes are stored here
        putInCache(attribute.getLocalName(), attribute.getNodeValue());
      }
    }

  }

  private void putInCache(String prefix, String uri) {
    prefix2Uri.put(prefix, uri);
    uri2Prefix.put(uri, prefix);
  }
}