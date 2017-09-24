(ns vocab-to-graphviz.graphviz
  (:require [vocab-to-graphviz.util :as util]
            [vocab-to-graphviz.spec :as spec]
            [hiccup.core :refer [html]]
            [dorothy.core :refer :all])
  (:import (java.awt GraphicsEnvironment)))

(def ^:private font-available?
  "Predicate testing what fonts are available on the given system."
  (set (.getAvailableFontFamilyNames (GraphicsEnvironment/getLocalGraphicsEnvironment))))

(def ^:private monospace-font
  (or (some font-available? ["Monaco"]) "Monospace"))

(def ^:private sans-serif-font
  (or (some font-available? ["Open Sans"]) "Sans-Serif"))

(def ^:private small-font
  {:point-size 10})

(def ^:private large-font
  {:point-size 14})

(def ^:private to-key
  (comp keyword (partial str "k") util/sha1))

(defn datatype-property-node
  "Format a datatype property."
  [{:keys [property]
    range-iri :range}]
  [:tr [:td {:align "left"} [:font small-font property]]
   [:td {:align "right"} [:font (assoc small-font :color "dimgrey") range-iri]]])

(defn class-node
  "Format a node for class."
  [{class-iri :class
    :keys [datatype-properties internal?]
    :as node}]
  (let [class-key (to-key class-iri)]
    [class-key
     {:label (html (into [:table {:border 1 :cellborder 0 :port class-key}
                          [:tr [:td {:colspan 2} [:font large-font class-iri]]]
                          [:hr]]
                         (if datatype-properties
                           (map datatype-property-node datatype-properties)
                           ; Placeholder for classes with no datatype properties.
                           ; Without the placeholder, the class is not displayed.
                           [[:tr [:td {:colspan 2} " "]]])))
      :fontname monospace-font}]))

(defn edges
  "Format edges representing object properties of classes."
  [object-properties]
  (letfn [(format-label [label]
            [:tr [:td {:align "left"} [:font small-font label]]])
          (format-edge [[[domain range-iri] properties]]
            [(to-key domain)
             (to-key range-iri)
             {:label (html (into [:table {:border 0 :cellborder 0}]
                                 (mapv (comp format-label :property)
                                       (sort-by :property properties))))
              :arrowhead "open"
              :fontname monospace-font}])]
    (->> object-properties
      (group-by (juxt :domain :range))
      (map format-edge))))

(defn namespace-prefixes-box
  "Make a box with namespace prefixes used in a diagram."
  [ns-prefixes]
  (html (into [:table {:border 1 :cellborder 0}
               [:tr [:td {:colspan 2} [:font (assoc large-font :face sans-serif-font) "Namespace prefixes"]]]
               [:hr]]
              (for [[ns-iri prefix] (sort-by second ns-prefixes)]
                [:tr [:td {:align "right"} [:font small-font
                                             (if (pos? (count prefix)) prefix " ")]]
                     [:td {:align "left"} [:font small-font ns-iri]]]))))

(defn class-diagram
  "Make a class diagram representing `schema`.
  `ns-prefixes` are used as a legend explaining the namespace prefixes."
  [ns-prefixes
   {:keys [classes object-properties subclasses]
    :as schema}]
  (dot (digraph (concat [{:overlap "false"
                          :splines "true"}
                         (node-attrs {:shape :none
                                      :margin 0})]
                        (map class-node classes)
                        (edges object-properties)
                        [(subgraph :legend [{:rank :sink}
                                            [:prefixes {:label (namespace-prefixes-box ns-prefixes)
                                                        :fontname monospace-font}]])]))))
