(defproject kindlemail "1.0.0-SNAPSHOT"
  :description "A simple utility for emailing files/webpages to a Kindle device."
  :author "Curtis Wolterding"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [com.draines/postal "1.8.0"]
                 [org.clojure/tools.cli "0.2.1"]]
  :main kindlemail.core)