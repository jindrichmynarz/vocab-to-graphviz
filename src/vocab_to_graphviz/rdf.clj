(ns vocab-to-graphviz.rdf
  (:import (org.apache.jena.rdf.model Literal Resource)
           (org.apache.jena.datatypes DatatypeFormatException)))

(defprotocol RDFResource
  "Returns a Clojuresque representation of an RDF resource"
  (resource->clj [resource]))

(extend-protocol RDFResource
  Literal
  (resource->clj [resource]
    (let [datatype (.getDatatypeURI resource)]
      (try (.getValue resource)
           (catch DatatypeFormatException _
             ; Treat invalid literals as strings
             (.getLexicalForm resource)))))

  Resource
  (resource->clj [resource]
    (if (.isAnon resource)
      (str "_:" (.getId resource))
      (.getURI resource)))

  nil
  (resource->clj [_] nil))
