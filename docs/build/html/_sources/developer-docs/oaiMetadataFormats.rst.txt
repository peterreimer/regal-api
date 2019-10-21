Neues Metadaten-Format in die OAI-Schnittstelle integrieren
===========================================================

Die Integration eines neuen Metadatenformats in die OAI-Schnittstelle umfasst Aktivitäten an mehreren Stellen.

1. Java-Klassen erweitern und anpassen
2. Konfiguration des OAI-Providers anpassen
3. Testen der Schnittstelle

Java-Klassen erweitern und anpassen
-----------------------------------

Für die Integration eines neuen Metadaten-Formats in die OAI-Schnittstelle sind zunächst die folgenden Dateien relevant 

* regal-api.app.helper.oai/\*
* regal-api.app.helper.oai/\*Mapper.java
* regal-api.app.helper.oai/OaiDispatcher.java
* regal-api.app.models/DublinCoreData.java


Zunächst kopiere ich die Datei WGLMapper.java und erstelle damit die Datei OpenAireMapper.java
Die Datei DublinCoreData.java kopiere ich und erstelle damit OpenAireData.java

In der Datei OaiDispatcher.java muss ein zusätzlicher Transformer-Aufruf generiert werden und eine neue Methode addOpenaireTransformer erstellt werden. 

.. code:: java

    private static void addOpenAireTransformer(Node node) {
      String type = node.getContentType();
        if ("public".equals(node.getPublishScheme())) {
          if ("monograph".equals(type) || "journal".equals(type)
            || "webpage".equals(type) || "researchData".equals(type)
            || "article".equals(type)) {
              node.addTransformer(new Transformer("openaire"));
          }
        }
      } 


Ebenso muss in die Methode addUnknownTransformer eine zusätzliche If-Abfrage integriert werden.

.. code:: java

    private static void addUnknownTransformer(List<String> transformers,
      Node node) {
      if (transformers != null) {
        for (String t : transformers) {
          if ("oaidc".equals(t))
            continue; // implicitly added - or not allowed to set
          if ("epicur".equals(t))
            continue; // implicitly added - or not allowed to set
          if ("aleph".equals(t))
            continue; // implicitly added - or not allowed to set
          if ("mets".equals(t))
            continue; // implicitly added - or not allowed to set
          if ("rdf".equals(t))
            continue; // implicitly added - or not allowed to set
          if ("wgl".equals(t))
            continue; // implicitly added - or not allowed to set
          if ("openaire".equals(t))
            continue; // implicitly added - or not allowed to set
		  node.addTransformer(new Transformer(t));
        }
      }
    }


Konfiguration des OAI-Providers anpassen
----------------------------------------

In der Datei proai.properties müssen die mit der OAI-Schnittstelle zusammenhängenden Konfigurationen angepasst werden. Die Datei wird direkt im entpackten Applikation-Container angepasst. 

.. code:: bash

    ################################################
    # Fedora Driver: Metadata Format Configuration #
    ################################################
    # Metadata formats to make available.
    #
    # This is a space-delimited list of all formats provided,
    # identified by OAI metadata prefix.
    #
    driver.fedora.md.formats = oai_dc epicur mabxml-1 mets rdf oai_wgl oai_openaire
    #oai_dc oai_epicur oai_ore
    # The location of the W3C schema for each format.
    #
    # Example property name: 
    # <code>driver.fedora.md.format.your_format.loc</code>.
    #
    driver.fedora.md.format.oai_ore.loc = http://www.w3.org/2000/07/rdf.xsd
    driver.fedora.md.format.oai_dc.loc = http://www.openarchives.org/OAI/2.0/oai_dc.xsd
    driver.fedora.md.format.epicur.loc = http://www.persistent-identifier.de/xepicur/version1.0/xepicur.xsd
    driver.fedora.md.format.mabxml-1.loc = http://files.dnb.de/standards/formate/mabxml-1.xsd
    driver.fedora.md.format.mets.loc = http://www.loc.gov/standards/mets/mets.xsd
    driver.fedora.md.format.rdf.loc = http://ilrt.org/discovery/2001/09/rdf-xml-schema/rdf.xsd
    driver.fedora.md.format.oai_wgl.loc = http://www.leibnizopen.de/fileadmin/default/documents/oai_wgl/oai_wgl.xsd
    driver.fedora.md.format.oai_openaire.loc = https://www.openaire.eu/schema/repo-lit/4.0/openaire.xsd
    driver.fedora.md.format.test_format.loc = http://example.org/test_format.xsd
    driver.fedora.md.format.formatX.loc = http://example.org/formatX.xsd
    driver.fedora.md.format.formatY.loc = http://example.org/formatY.xsd
    # The namespace URI for each format. 
    #
    # Example property name: 
    # <code>driver.fedora.md.format.your_format.uri</code>.
    #
    driver.fedora.md.format.oai_ore.uri = http://www.w3.org/1999/02/22-rdf-syntax-ns#
    driver.fedora.md.format.oai_dc.uri = http://www.openarchives.org/OAI/2.0/oai_dc/
    driver.fedora.md.format.epicur.uri = urn:nbn:de:1111-2004033116
    driver.fedora.md.format.mabxml-1.uri = http://files.dnb.de/standards/formate/
    driver.fedora.md.format.mets.uri = http://www.loc.gov/standards/mets/
    driver.fedora.md.format.rdf.uri = http://ilrt.org/discovery/2001/09/rdf-xml-schema/
    driver.fedora.md.format.oai_wgl.uri = http://www.leibnizopen.de/fileadmin/default/documents/oai_wgl/
    driver.fedora.md.format.oai_openaire.uri = http://namespace.openaire.eu/schema/oaire/
    driver.fedora.md.format.test_format.uri = http://example.org/test_format/
    driver.fedora.md.format.formatX.uri = http://example.org/formatX/
    driver.fedora.md.format.formatY.uri = http://example.org/formatY/
    # The Fedora dissemination type for each format.
    #
    # <p>A Fedora dissemination type is a URI starting with 
    # <code>info:fedora/*/</code> and ending with a datastream ID (such as "DC"), 
    # a Behavior Definition PID followed by "/methodName", 
    # or a Behavior Definition PID followed by "/methodName?name=value".</p>
    #
    # <p>The dissType is the key to mapping an OAI metadata format to
    # a kind of Fedora dissemination.  Here are a few examples:</p>
    #
    # <pre>
    # info:fedora/*/DC                  ; identifies the "DC" datastream
    #
    # info:fedora/*/demo:1/getDC        ; identifies the "getDC" method of the 
    #                                   ; demo:1 behavior definition
    #
    # info:fedora/*/demo:1/getMD?fmt=dc ; identifies the "getMD" method of the 
    #                                   ; demo:1 behavior definition, when
    #
    # info:fedora/*/demo:1/getMD?fmt=dc ; identifies the "getMD" method of the 
    #                                   ; demo:1 behavior definition, when
    #                                   ; invoked with the required "fmt"
    #                                   ; parameter
    # </pre>
    #
    # When the OAI provider queries Fedora for records in your_format, 
    # it uses this special value to constrain the query to only those
    # disseminations that are in the expected format.
    # Thus, all records that the OAI provider considers to be in
    # your_format must have this dissemination type.
    #
    # Example property name: 
    # <code>driver.fedora.md.format.your_format.dissType</code>.
    #
    driver.fedora.md.format.oai_dc.dissType = info:fedora/*/CM:oaidcServiceDefinition/oaidc
    driver.fedora.md.format.mabxml-1.dissType = info:fedora/*/CM:alephServiceDefinition/aleph
    driver.fedora.md.format.epicur.dissType = info:fedora/*/CM:epicurServiceDefinition/epicur
    driver.fedora.md.format.mets.dissType = info:fedora/*/CM:metsServiceDefinition/mets
    driver.fedora.md.format.rdf.dissType = info:fedora/*/CM:rdfServiceDefinition/rdf
    driver.fedora.md.format.oai_wgl.dissType = info:fedora/*/CM:wglServiceDefinition/wgl
    driver.fedora.md.format.oai_openaire.dissType = info:fedora/*/CM:openaireServiceDefinition/openaire
    driver.fedora.md.format.oai_ore.dissType = info:fedora/*/ellinet:EllinetObjectServiceDefinition/oai_ore
    driver.fedora.md.format.test_format.dissType = info:fedora/*/test_format
    driver.fedora.md.format.formatX.dissType = info:fedora/*/demo:OAIAdvancedItem-Service/getMetadata?format=x
    driver.fedora.md.format.formatY.dissType = info:fedora/*/demo:OAIAdvancedItem-Service/getMetadata?format=y
    # The Fedora dissemination type for each format.
    #
    # This optional property identifies the OAI "about" dissemination 
    # type for your_format. If specified for your_format, then the OAI provider
    # will attempt to find disseminations of this type for each object
    # that has a matching your_format.dissType.  If such a dissemination
    # is found, for that particular object, the information therein
    # will be used as the "about" metadata for the record.
    #
    # Example property name: 
    # <code>driver.fedora.md.format.your_format.about.dissType</code>.
    #
    driver.fedora.md.format.oai_dc.about.dissType = info:fedora/*/about_oai_dc
    driver.fedora.md.format.formatX.about.dissType = info:fedora/*/demo:OAIAdvancedItem-Service/getMetadataAbout?format=x
    driver.fedora.md.format.formatY.about.dissType = info:fedora/*/demo:OAIAdvancedItem-Service/getMetadataAbout?format=y


Testen der Schnittstelle
------------------------

Die OAI-Schnittstelle ist über die URL http://api.ellinet-dev.hbz-nrw.de/oai-pmh/ oder analog bei edoweb-test erreichbar.

