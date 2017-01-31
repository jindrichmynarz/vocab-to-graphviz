(ns vocab-to-graphviz.core
  (:require [vocab-to-graphviz.sparql :as sparql]
            [vocab-to-graphviz.prefix :as prefix]
            [vocab-to-graphviz.util :as util]
            [clojure.string :as string]))

(defn classes
  "Get classes in the vocabulary."
  []
  (sparql/select-query "classes.rq"))

(defn properties
  "Get properties in the vocabulary."
  []
  (sparql/select-query "properties.rq"))

(defn subclasses
  "Get rdfs:subClassOf links in the vocabulary."
  []
  (sparql/select-query "subclasses.rq"))

(defn get-internal-terms
  "Returns a predicate testing whether term from the vocabulary."
  []
  (into #{} (map :term (sparql/select-query "internal_terms.rq"))))

(defn schema
  "Generate schema of the vocabulary."
  []
  (let [remove-datatype-property-annotation (fn [property] (dissoc property :isDatatypeProperty))
        remove-domain (fn [property] (dissoc property :domain))
        properties' (properties)
        subclasses' (subclasses)
        internal-term? (get-internal-terms)
        ->class (partial hash-map :class)
        datatype-property-domains (->> properties'
                                       (filter :isDatatypeProperty)
                                       (map (comp ->class :domain)))
        object-property-domain-ranges (->> properties'
                                           (remove :isDatatypeProperty)
                                           (mapcat (juxt :domain :range))
                                           (map ->class))
        classes' (for [{class-iri :class} (distinct (concat (classes)
                                                            datatype-property-domains
                                                            object-property-domain-ranges))
                       :let [class-datatype-property? (every-pred (comp (partial = class-iri) :domain)
                                                                  :isDatatypeProperty)
                             datatype-properties (->> properties'
                                                      (filter class-datatype-property?)
                                                      (map (comp remove-domain
                                                                 remove-datatype-property-annotation))
                                                      (sort-by :property))]]
                   (cond-> {:class class-iri
                            :internal? (boolean (internal-term? class-iri))}
                     (seq datatype-properties) (assoc :datatype-properties datatype-properties)))
        object-properties (->> properties'
                               (remove :isDatatypeProperty)
                               (map remove-datatype-property-annotation))]
    (cond-> {}
      (seq classes') (assoc :classes classes')
      (seq object-properties) (assoc :object-properties object-properties)
      (seq subclasses') (assoc :subclasses subclasses'))))

(defn schema-terms
  "Extract T-Box terms from `schema`."
  [{:keys [classes object-properties subclasses]}]
  (concat (mapcat (juxt :subclass :superclass) subclasses)
          (mapcat (juxt :property :domain :range) object-properties)
          (flatten (mapcat (juxt :class (comp (partial map :range) :datatype-properties)) classes))))

(defn namespaces
  "Get distinct namespaces from `schema-terms`."
  [schema-terms]
  (distinct (remove nil? (map (comp first prefix/split-local-name) schema-terms))))

(defn most-frequent-namespace
  "Get the most frequent namespace from `schema-terms`."
  [schema-terms]
  (->> schema-terms
       (map (comp first prefix/split-local-name))
       (remove nil?)
       frequencies
       (sort-by (comp - second))
       (map first)
       (filter (partial not= (prefix/xsd))) ; Skip XML Schema
       first))

(defn namespace-prefixes
  "Create a namespace-prefix list from `schema-terms`."
  [schema-terms]
  (let [top-namespace (most-frequent-namespace schema-terms)
        conventions (prefix/prefix-conventions)
        namespace->prefix (fn [[prefix-map index] ns-iri]
                            (cond ; Top namespace has empty prefix
                                  (= ns-iri top-namespace)
                                    [(assoc prefix-map ns-iri "") index]
                                  ; Get conventional prefix
                                  (contains? conventions ns-iri)
                                    [(assoc prefix-map ns-iri (conventions ns-iri)) index]
                                  ; Mint new prefix
                                  :else
                                    [(assoc prefix-map ns-iri (str "ns" index)) (inc index)]))
        longer (fn [a b] (> (count a) (count b)))
        init-map {(prefix/rdfs) "rdfs"}]
    (->> schema-terms
         namespaces
         (reduce namespace->prefix [init-map 0])
         first
         (map vec)
         (sort-by first longer)))) ; Longer namespaces first

(defn compact-iri
  "Compact an `iri` using namespace-prefix mappings from `ns-prefixes`."
  [ns-prefixes iri]
  (if-let [[ns-iri prefix] (first (filter (comp (partial string/starts-with? iri) first) ns-prefixes))]
    (str prefix \: (subs iri (count ns-iri)))
    iri))

(defn compact-schema-iris
  "Compact IRIs in `schema` using `ns-prefix-map` that maps namespaces to prefixes."
  [ns-prefixes {:keys [classes object-properties subclasses]}]
  (let [compact-iri' (partial compact-iri ns-prefixes)
        compact-property (fn [property]
                           (util/update-keys property [:property :domain :range] compact-iri'))]
    {:subclasses (map (fn [subclass]
                        (util/update-keys subclass [:subclass :superclass] compact-iri'))
                      subclasses) 
     :object-properties (map compact-property object-properties)
     :classes (map (fn [{class-iri :class
                         properties :datatype-properties}]
                     (cond-> {:class (compact-iri' class-iri)}
                       (seq properties) (assoc :datatype-properties (map compact-property properties))))
                   classes)}))
