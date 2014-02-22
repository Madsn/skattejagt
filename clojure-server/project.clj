(defproject
  clojure-server
  "0.1.0-SNAPSHOT"
  :repl-options
  {:init-ns clojure-server.repl}
  :dependencies
  [[ring-server "0.3.1"]
   [domina "1.0.2"]
   [com.novemberain/monger "1.7.0"]
   [com.taoensso/timbre "3.0.0"]
   [environ "0.4.0"]
   [markdown-clj "0.9.41"]
   [prismatic/dommy "0.1.2"]
   [org.clojure/clojurescript "0.0-2138"]
   [http-kit "2.1.13"]
   [com.taoensso/tower "2.0.1"]
   [org.clojure/clojure "1.5.1"]
   [log4j
    "1.2.17"
    :exclusions
    [javax.mail/mail
     javax.jms/jms
     com.sun.jdmk/jmxtools
     com.sun.jmx/jmxri]]
   [cljs-ajax "0.2.3"]
   [lib-noir "0.8.0"]
   [compojure "1.1.6"]
   [selmer "0.5.9"]]
  :cljsbuild
  {:builds
   [{:source-paths ["src-cljs"],
     :compiler
     {:pretty-print false,
      :output-to "resources/public/js/site.js",
      :optimizations :advanced}}]}
  :ring
  {:handler clojure-server.handler/app,
   :init clojure-server.handler/init,
   :destroy clojure-server.handler/destroy}
  :cucumber-feature-paths
  ["test/features/"]
  :profiles
  {:uberjar {:aot :all},
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}},
   :dev
   {:dependencies
    [[org.clojure/core.cache "0.6.3"]
     [ring/ring-devel "1.2.1"]
     [clj-webdriver/clj-webdriver "0.6.1"]
     [ring-mock "0.1.5"]],
    :env {:dev true}}}
  :url
  "http://example.com/FIXME"
  :main
  clojure-server.core
  :plugins
  [[lein-ring "0.8.10"]
   [lein-environ "0.4.0"]
   [lein-cucumber "1.0.2"]
   [lein-cljsbuild "0.3.3"]]
  :description
  "FIXME: write description"
  :min-lein-version "2.0.0")