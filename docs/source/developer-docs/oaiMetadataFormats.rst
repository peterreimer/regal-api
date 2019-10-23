Neues Metadaten-Format in die OAI-Schnittstelle integrieren
===========================================================

Die Integration eines neuen Metadatenformats in die OAI-Schnittstelle umfasst Aktivitäten an mehreren Stellen.

1. Java-Klassen erweitern und anpassen
2. Konfiguration der regal-api und des OAI-Providers anpassen
3. Testen der Schnittstelle

Java-Klassen erweitern und anpassen
-----------------------------------

Für die Integration eines neuen Metadaten-Formats in die OAI-Schnittstelle sind zunächst die folgenden Dateien relevant 

* regal-api.app.helper.oai/\*
* regal-api.app.helper.oai/\*Mapper.java
* regal-api.app.helper.oai/OaiDispatcher.java
* regal-api.app.models/DublinCoreData.java
* regal-api.app.actions/Transform.java
* regal-api.app.controllers/Resource.java


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
      [...]
          if ("openaire".equals(t))
            continue; // implicitly added - or not allowed to set
          node.addTransformer(new Transformer(t));
        }
      }
    }


Die Datei Transform muss um eine Methode openaire erweitert werden. Diese Methode wird später über eine in der Datei Resource.java definierte ApiOperation "asOpenAire" als Restful Request aufgerufen. Die ApiOperation muss entsprechend auch angelegt werden.  
Innerhalb des Packages view.oai habe ich die neuen Klassen openaire.scala.html und openaireView.scala.html angelegt, die nahc meinem Verständnis die Darstellung des openaire-Objektes steuern sollen.   


Konfiguration der regal-api und des OAI-Providers anpassen
----------------------------------------------------------

Damit das als Dissemination* angelegte neue Format über die regal-api abgefragt werden kann muss in der Datei conf/routes eine entsprechende Konfirgurationszeile erstellt werden.

.. code:: bash

    GET /resource/:pid.openaire	    controllers.Resource.asOpenAire(pid, validate : Boolean ?= false)

Mit dieseem Eintrag wird eine Verbindung zwischen der entsprechenden Java-Methode und dem über das Play Framework stattfindenden Aufruf über eine HTTP-Methode erreicht.  

Wie zu sehen ist, wird hier auch bestimmt, ob das erstellte Objekt normalerweise gegen eine xsd-Datei validiert werden soll. Im Beispile ist das nicht der Fall: validate : Boolean ?= false. 
In der Datei proai.properties müssen die mit der OAI-Schnittstelle zusammenhängenden Konfigurationen angepasst werden. Die Datei wird direkt im entpackten Applikation-Container angepasst. 

.. code:: bash

    ################################################
    # Fedora Driver: Metadata Format Configuration #
    ################################################
    # Metadata formats to make available.
    driver.fedora.md.formats = oai_dc epicur mabxml-1 mets rdf oai_wgl oai_openaire
    [...]
    driver.fedora.md.format.oai_ore.loc = http://www.w3.org/2000/07/rdf.xsd
    
    driver.fedora.md.format.oai_openaire.loc = https://www.openaire.eu/schema/repo-lit/4.0/openaire.xsd
    
    [...]

    driver.fedora.md.format.oai_ore.uri = http://www.w3.org/1999/02/22-rdf-syntax-ns#
    
    driver.fedora.md.format.oai_openaire.uri = http://namespace.openaire.eu/schema/oaire/
    
    [...]

    driver.fedora.md.format.oai_dc.dissType = info:fedora/*/CM:oaidcServiceDefinition/oaidc
    
    driver.fedora.md.format.oai_openaire.dissType = info:fedora/*/CM:openaireServiceDefinition/openaire
    


Testen der Schnittstelle
------------------------

Die OAI-Schnittstelle ist über die URL http://api.ellinet-dev.hbz-nrw.de/oai-pmh/ oder analog bei edoweb-test erreichbar.
Der neue ServiceDisseminator kann über die regal-api aufgerufen werden, wenn der in der routes Datei deklarierte Pfad entsprechend aufgerufen wird. Obwohl GET als Methode deklariert ist, funktioniert jedoch nur der Aufruf mittels POST. Deshalb kommt cUrl zum Einsatz: curl -XGET -uedoweb-admin localhost:9000/resource/frl%3A6402576.openaire
