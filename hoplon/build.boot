#!/usr/bin/env boot

#_#tailrecursion.boot.core/version "2.5.0"
#tailrecursion.boot.core/version "2.5.1"

(set-env!
 :project      'berest-hoplon-client
 :version      "0.1.0-SNAPSHOT"

 #_:repositories #_{"my.datomic.com" {:url "https://my.datomic.com/repo"
                                  :username "michael.berg@zalf.de"
                                  :password "dfe713b3-62f0-469d-8ac9-07d6b02b0175"}
                "jboss" "https://repository.jboss.org/nexus/content/groups/public/"
                }

 :dependencies '[#_[tailrecursion/boot.task   "2.2.3"]
                 [tailrecursion/boot.task   "2.2.4"]
                 #_[tailrecursion/hoplon      "5.10.14"]
                 [tailrecursion/hoplon      "5.10.24"]
                 [tailrecursion/boot.notify "2.0.2"]
                 [tailrecursion/boot.ring   "0.2.1"]
                 #_[org.clojure/clojurescript "0.0-2202"]

                 [cljs-ajax "0.2.3"]

                 #_[org.clojure/core.match "0.2.1"]

                 #_[com.datomic/datomic-pro "0.9.4899"]

                 #_[buddy "0.1.0-beta4"]
                 #_[crypto-password "0.1.1"]

                 #_[ring "1.2.1"]
                 #_[fogus/ring-edn "0.2.0"]

                 #_[hiccup "1.0.4"]

                 #_[simple-time "0.1.1"]
                 #_[clj-time "0.6.0"]
                 [com.andrewmcveigh/cljs-time "0.1.5"]

                 #_[clojure-csv "2.0.1"]
                 #_[org.clojure/algo.generic "0.1.1"]
                 #_[org.clojure/math.numeric-tower "0.0.2"]
                 #_[com.taoensso/timbre "3.1.6"]
                 #_[org.clojars.pallix/analemma "1.0.0"]
                 #_[org.clojure/core.match "0.2.0"]
                 #_[com.keminglabs/c2 "0.2.3"]
                 #_[formative "0.3.2"]
                 #_[com.velisco/clj-ftp "0.3.0"]
                 #_[instaparse "1.3.2"]
                 #_[org.clojure/tools.logging "0.2.6"]
                 #_[org.clojure/tools.namespace "0.2.4"]
                 #_[clojurewerkz/propertied "1.1.0"]
                 ]
 :out-path     "../../berest-service/castra/resources/public"
 :src-paths    #{"src/hl"
                 "src/cljs"
                 "src/apogee"

                 ;for castra in dev mode
                 #_"../../berest-service/castra/src"
                 #_"../../berest-core/private-resources"
                 #_"../../berest-core/src"})

;; Static resources (css, images, etc.):
(add-sync! (get-env :out-path) #{"assets"})

(require '[tailrecursion.hoplon.boot :refer :all]
         '[tailrecursion.boot.task.notify :refer [hear]]
         '[tailrecursion.castra.task :as c])

(deftask development
         "Build BEREST Hoplon client for development."
         []
         (comp (watch)
               (hear)
               (hoplon {:prerender false :pretty-print true})
               #_(c/castra-dev-server 'de.zalf.berest.web.castra.api)))

(deftask dev-sourcemaps
         []
         (comp
           (watch)
           (hear)
           (hoplon {:prerender false :pretty-print true :source-map true})
           #_(c/castra-dev-server 'de.zalf.berest.web.castra.api)
           #_(dev-server)))

(deftask production
  "Build BEREST hoplon client for production."
  []
  (hoplon {:optimizations :advanced}))
