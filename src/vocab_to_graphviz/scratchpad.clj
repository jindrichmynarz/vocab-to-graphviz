(ns vocab-to-graphviz.scratchpad
  (:require [vocab-to-graphviz.core :as core]
            [vocab-to-graphviz.graphviz :as graphviz]
            [vocab-to-graphviz.spec :as spec]
            [mount.core :as mount]
            [dorothy.core :refer :all]
            [clojure.pprint :refer [pprint]]))

(comment
  (def params {::spec/input "public-contracts.ttl"
               ::spec/output *out*})
  (mount/start-with-args params)
  (mount/stop)
  (filter (comp (partial = "http://purl.org/procurement/public-contracts#criterionWeight") :property)
          (core/properties))
  (def schema (core/schema))
  (pprint (:classes schema))
  (def ns-prefixes (core/namespace-prefixes (core/schema-terms schema)))
  (def compact-schema (core/compact-schema-iris ns-prefixes schema))
  (pprint (:classes compact-schema))
  (def diagram (graphviz/class-diagram compact-schema ns-prefixes))
  (spit "diagram.dot" diagram)
  (show! (dot diagram))
  )
