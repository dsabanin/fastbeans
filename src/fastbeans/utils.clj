(ns fastbeans.utils
  (:use simplelog.use)
  (:require
    [clojure.java.io :as io])
  (:import
   [java.io File]
   [org.apache.commons.io FileUtils]))

(defmacro dofuture
  [f-str & body]
  `(do
     (future
       (try
         (benchmark ~f-str
                    (info "Background call" ~f-str)
                    ~@body)
         (catch Exception e#
           (error "Failed on background call" ~(str *ns* "/" f-str))
           (print-exception e#))))
     :bg:spawned))
