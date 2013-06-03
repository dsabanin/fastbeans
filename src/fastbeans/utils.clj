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

(defn join-paths
  [& paths]
  (reduce #(.getPath (File. %1 %2)) paths))

(defn file-exists?
  [path]
  (.exists (io/as-file path)))

(defn touch
  [path]
  (FileUtils/touch (io/as-file path)))
