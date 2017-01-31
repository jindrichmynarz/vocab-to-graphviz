(defproject vocab-to-graphviz "0.1.0-SNAPSHOT"
  :description "Visualize RDF vocabularies via Graphviz"
  :url "https://github.com/jindrichmynarz/vocab-to-graphviz"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/tools.cli "0.3.5"]
                 [mount "0.1.11"]
                 [slingshot "0.12.2"]
                 [stencil "0.5.0"]
                 [commons-validator/commons-validator "1.5.1"]
                 [cheshire "5.7.0"]
                 [hiccup "1.0.5"]
                 [dorothy "0.0.6"]
                 [org.apache.jena/jena-core "3.1.1"]
                 [org.apache.jena/jena-arq "3.1.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]]
  :main vocab-to-graphviz.cli
  :profiles {:dev {:plugins [[lein-binplus "0.4.2"]]}
             :uberjar {:aot :all
                       :uberjar-name "vocab_to_graphviz.jar"}}
  :bin {:name "vocab_to_graphviz"})
