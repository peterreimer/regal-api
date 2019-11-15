JSON in Java-Klassen überführen
===============================

Mit der Klasse JsonLDMapper steht ein generischer Ansatz zum Einlesen des lobid-JSON (oder anderer JSON-Formate) zur Verfügung, 
der die im JSON liegenden Metadaten in einer einheitlichen Weise für die Verarbeitung zugänglich macht.

Grundlage bilden die von JSON unterstützten Datentypen. Die in JSON verwendeten Datentypen werden zunächst konzeptuell auf drei Typen reduziert.

- Object
- Array of Values als ArrayList<Hashtable\<String,String\>>
- Key\/Value-Paare als Hashtable\<String,String\>

Der Datentyp Object wird als Container-Element für weitere Datentypen verwendet und rekursiv bis zu den 
elementaren Datentypen Array of Values und key/value-Paare aufgelöst. Die dabei verarbeitete Pfad-Struktur wird 
in der Java-Notation abgebildet und in einem String abgelegt..

Für alle auf den beiden Datentypen Array of Values und Key\/Value-Paare aufbauenden Objekte bietet die Mapper-Klasse vereinheitlichte Instanzen mit analogen Zugriffsmethoden
an. Der JsonLDMapper bietet jeweils die Methode getElement(Pfad), die transparent ArrayList<Hashtable\<String,String\> zurückliefert. 

Über die Iteration über die jeweilige ArrayList stehen damit entweder zusammengehörende Key\/Value zur Verfügung, oder die einzelnen Values eines 
Arrays of Value in Form des Array-Bezeichners aus dem JSON und des jeweiligen Wertes.

Besipiele

 Für 
 
 .. code:: bash
 
 	{record : {title: [\"Ausdrücke in Java\", "\Java Expressions\", \"Expression de Java\"]}}
 
 erhält man durch:

 .. code:: java

	JsonLDMapper jMapper = new JsonLDMapper(JsonNode); 
	ArrayList<Hashtable<String,String> title = jMapper.getElement("\root.record.title\");
	
eine ArrayListe die aus drei Key/Value-Paaren besteht:

	title : \"Ausdrücke in Java\"
	title : "\Java Expressions\"
	title : \"Expression de Java\"
	
Für 

 .. code:: bash
 
 	{record : {creator: { 
 		prefLabel : \"Loki Schmidt\",
 		@id : \"https://yxz.org\" }
 	}}
	
.. code:: java

	JsonLDMapper jMapper = new JsonLDMapper(JsonNode); 
	ArrayList<Hashtable<String,String> title = jMapper.getElement("\root.creator\");
	
eine ArrayListe die aus zwei Key/Value-Paaren besteht:

	prefLabel : \"Loki Schmidt\"
	@id : \"https://yxz.org\"

  
Zusätzlich lässt sich mit den Methoden isArray() und isObject() feststellen, was der ursprüngliche Datentyp war. 

.. code:: java

	JsonLDMapper jMapper = new JsonLDMapper(JsonNode); 
	boolean test = jMapper.getElement("\root.creator\").isArray();
	
