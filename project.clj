(defproject com.oscaro/tools-io-sftp "0.1.0-SNAPSHOT"
  :description "SFTP Implementation for tools-io"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.oscaro/tools-io "0.3.23"]
                 [com.jcraft/jsch     "0.1.55"]]
  :profiles {:dev {:global-vars {*warn-on-reflection* true}
                   :plugins [[lein-codox "0.10.2"]]
                   :source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]]}}
  :repl-options {:init-ns user})
