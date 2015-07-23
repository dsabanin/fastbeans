(ns fastbeans.rpc
  (:use fastbeans.utils
        simplelog.use)
  (:require tiny-bugsnag.core
            [clj-stacktrace.repl :as stack]))

(def print-call-filters (atom #{}))

(defn auto-resolve
  "Resolves qualified symbol and tries to require namespace if missing."
  [f-str]
  (let [f-sym (symbol f-str)]
    (some-> f-sym namespace symbol require)
    (resolve f-sym)))

(def cached-auto-resolve (memoize auto-resolve))

(defn error?
  "Is reply an error reply?"
  [res]
  (and (map? res)
       (contains? res :fastbeans-error)))

(defn print-filter-pred
  [x]
  (some (fn [f] (try
                 (f x)
                 (catch Exception e
                   false))) @print-call-filters))

(defn print-filter
  [arg]
  (if (coll? arg)
    (->> arg
         (filter (complement print-filter-pred))
         (into (empty arg)))
    (when-not (print-filter-pred arg)
      arg)))

(defn add-print-filter! [pred]
  (swap! print-call-filters conj pred))

(defn die
  "Construct a death reply for client to know the error information."
  [error-id & args]
  (apply error (str "[" error-id "]") args)
  {:fastbeans-error error-id
   :error-information (print-filter args)})

(defn prn-call
  "Print call information."
  [f args]
  (str f " " (clojure.string/join " " (->> args
                                           (map print-filter)
                                           (map str)))))

(defn notify-bugsnag [exc f-str args signature]
  (tiny-bugsnag.core/notify :context (str "RPC call (" f-str ")")
                            :exception exc
                            :data {"Arguments" (merge (if (map? args)
                                                        args
                                                        {"Vector" (prn-str args)})
                                                      {"Signature" signature})}))

(defn dispatch
  "Dispatch incoming deserialized call and return the signature and result."
  [[[signature [f-str & args :as whole]]]]
  (try
    (if-let [f (cached-auto-resolve f-str)]
      (benchmark f-str
                 (info "Call" signature "-" (prn-call f args))
                 (let [res (apply f args)]
                   (if (error? res)
                     res
                     [signature res])))
      (die :failed-to-resolve f-str))
    (catch clojure.lang.ArityException e
      (print-exception e)
      (notify-bugsnag e f-str args signature)
      (die :wrong-arguments-exception (prn-call f-str args)))
    (catch Exception e
      (print-exception e)
      (notify-bugsnag e f-str args signature)
      (die :failed-with-exception {:call (prn-call f-str args)
                                   :message (.getMessage e)
                                   :backtrace (clojure.string/join "\n" (map str (.getStackTrace e)))}))))
