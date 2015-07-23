(defproject fastbeans "1.4.0-final1"
  :description "Clojure to Ruby RPC"
  :url "http://beanstalkapp.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :author "Dmitry Sabanin <sdmitry@gmail.com>"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [aleph "0.3.0-beta16" :exclusions [io.netty/netty]]
                 [clj-msgpack "0.2.0"]
                 [io.netty/netty "3.6.3.Final"]
                 [simplelog "1.0.13"]
                 [slingshot "0.10.3"]
                 [org.apache.commons/commons-io "1.3.2"]
                 [tiny-bugsnag "0.1.3"]]
  :aot [fastbeans.handlers])
