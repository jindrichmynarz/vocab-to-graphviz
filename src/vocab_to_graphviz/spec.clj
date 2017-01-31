(ns vocab-to-graphviz.spec
  (:require [clojure.spec :as s])
  (:import (java.io File Writer)))

(s/def ::file (partial instance? File))

(s/def ::help? true?)

(s/def ::input string?)

(s/def ::output (s/or :file ::file
                      :writer (partial instance? Writer)))

(s/def ::config (s/keys :req [::input ::output]
                        :opt [::help?]))
