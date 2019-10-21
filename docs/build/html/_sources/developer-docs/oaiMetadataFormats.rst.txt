Neues Metadaten-Format in die OAI-Schnittstelle integrieren
===========================================================

Für die Integration eines neuen Metadaten-Formats in die OAI-Schnittstelle scheinen besonders die folgenden Dateien relevant 

* regal-api.app.helper.oai/*
* regal-api.app.helper.oai/WglMapper.java
* regal-api.app.helper.oai/OaiDispatcher.java
* regal-api.app.models/DublinCoreData.java


Zunächst kopiere ich den WGLMapper.java und erstelle damit die Datei OpenAireMapper.java
Die Datei DublinCoreData.java kopiere ich und erstelle OpenAireData.java


In der Datei OaiDispatcher.java muss ein zusätzlicher Transformer-Aufruf generiert werden und anscheinend auch eine Methode addOpenaireTransformer erstellt werden. 
Mir scheint, dass sollte bei Gelegenheit durch eine Factory ersetzt werden.

OAI-Schnittstelle
-----------------

Die OAI-Schnittstelle ist über die URL http://api.ellinet-dev.hbz-nrw.de/oai-pmh/ oder analog bei edoweb-test erreichbar.
