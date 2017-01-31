(ns vocab-to-graphviz.store
  (:require [vocab-to-graphviz.spec :as spec]
            [mount.core :as mount])
  (:import (org.apache.jena.query Dataset DatasetFactory)
           (org.apache.jena.riot Lang RDFDataMgr)))

(defn- open-store
  [{::spec/keys [input]}]
  (let [dataset (DatasetFactory/create)
        owl (RDFDataMgr/loadModel "owl.ttl")
        vocab (RDFDataMgr/loadModel input)]
    (doto dataset
      (.addNamedModel "http://www.w3.org/2002/07/owl" owl)
      (.addNamedModel "urn:vocab" vocab))))

(defn- close-store
  [^Dataset store]
  (.close store))

(mount/defstate store
  :start (open-store (mount/args))
  :stop (close-store store))
