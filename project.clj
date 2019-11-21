(defproject fastbeans "1.4.0-final6"
  :description "Clojure to Ruby RPC"
  :url "http://beanstalkapp.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :author "Dmitry Sabanin <sdmitry@gmail.com>"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [aleph "0.3.0-beta16" :exclusions [io.netty/netty]]
                 [clojure-msgpack "1.2.1"]
                 [io.netty/netty "3.6.3.Final"]
                 [simplelog "1.0.13"]
                 [slingshot "0.10.3"]
                 [org.apache.commons/commons-io "1.3.2"]
                 [tiny-bugsnag "0.1.3"]
                 [org.apache.commons/commons-lang3 "3.4"]]
  :aot :all)
