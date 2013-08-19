(ns fastbeans.utils
  (:use simplelog.use))

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
