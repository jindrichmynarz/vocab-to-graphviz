(ns vocab-to-graphviz.sparql
  (:require [vocab-to-graphviz.store :refer [store]]
            [vocab-to-graphviz.rdf :refer [resource->clj]]
            [stencil.core :refer [render-file]])
  (:import (org.apache.jena.query Dataset)
           (org.apache.jena.query QueryExecutionFactory)))

; ----- Private functions -----

(defn- process-select-binding
  [sparql-binding variable]
  [(keyword variable) (resource->clj (.get sparql-binding variable))])

(defn- process-select-solution
  "Process SPARQL SELECT `solution` for `result-vars`."
  [result-vars solution]
  (into {} (mapv (partial process-select-binding solution) result-vars)))

; ----- Public functions -----

(defn template
  "Render the Mustache template from `path` using `data`."
  [path & {:keys [data]
           :or {data {}}}]
  (render-file path data))

(defn ask-query
  "Execute SPARQL ASK `query`."
  [^String query]
  (with-open [qexec (QueryExecutionFactory/create query store)]
    (.execAsk qexec)))

(defn select-query
  "Execute SPARQL SELECT `query`."
  [^String query]
  (with-open [qexec (QueryExecutionFactory/create query store)]
    (let [results (.execSelect qexec)
          result-vars (.getResultVars results)]
      (mapv (partial process-select-solution result-vars)
            (iterator-seq results)))))
