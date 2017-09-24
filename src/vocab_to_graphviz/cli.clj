(ns vocab-to-graphviz.cli
  (:gen-class)
  (:require [vocab-to-graphviz.core :as core]
            [vocab-to-graphviz.spec :as spec]
            [vocab-to-graphviz.util :as util]
            [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.spec :as s]
            [mount.core :as mount]))

; ----- Private functions -----

(defn- usage
  [summary]
  (util/join-lines ["Visualize RDF vocabularies via Graphviz"
                    ""
                    "Usage: vocab_to_graphviz [options]"
                    ""
                    "Options:"
                    summary]))

(defn- error-msg
  [errors]
  (util/join-lines (cons "The following errors occurred while parsing your command:\n" errors)))

(defn- validate-params
  [params]
  (when-not (s/valid? ::spec/config params)
    (util/die (str "The provided arguments are invalid.\n\n"
                   (s/explain-str ::spec/config params)))))

(defn save-diagram
  "Save DOT `diagram` to `output`."
  [output diagram]
  (if (= output *out*)
    (do (.write output diagram) (flush))
    (with-open [writer (io/writer output)]
      (.write writer diagram))))

(defn- main
  [{::spec/keys [output]
    :as params}]
  (validate-params params)
  (mount/start-with-args params)
  (save-diagram output (core/vocab->graphviz)))

; ----- Private vars -----

(def ^:private cli-options
  [["-i" "--input INPUT" "Path to the input vocabulary"
    :id ::spec/input
    :validate [util/file-exists? "The provided input file doesn't exist!"]]
   ["-o" "--output OUTPUT" "Path to the output file"
    :id ::spec/output
    :parse-fn io/as-file
    :default *out*
    :default-desc "STDOUT"]
   ["-h" "--help" "Display help information"
    :id ::spec/help?]])

; ----- Public functions -----

(defn -main
  [& args]
  (let [{{::spec/keys [help?]
          :as params} :options
         :keys [errors summary]} (parse-opts args cli-options)]
    (cond help? (util/info (usage summary))
          errors (util/die (error-msg errors))
          :else (main params))))
