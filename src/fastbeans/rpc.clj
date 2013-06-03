(ns fastbeans.rpc
  (:use
   fastbeans.utils
   simplelog.use))

(defn auto-resolve
  [f-str]
  (let [sym (symbol f-str)]
    (if-let [f-ns (-> sym namespace symbol)]
      (require f-ns))
    (resolve sym)))

(def cached-auto-resolve (memoize auto-resolve))

(defn die
  [error-id & args]
  (apply error (str "[" error-id "]") args)
  {:fastbeans-error error-id
   :error-information args})

(defn prn-call
  [f args]
  (str f " " (clojure.string/join " " (map str args))))

(defn dispatch [[[f-str & args :as whole]]]
  (try
    (if-let [f (cached-auto-resolve f-str)]
      (benchmark f-str
                 ; (info "Call:" (prn-call f args))
                 (apply f args))
      (die :failed-to-resolve f-str))
    (catch clojure.lang.ArityException e
      (print-exception e)
      (die :wrong-arguments-exception (prn-call f-str args)))
    (catch Exception e
      (print-exception e)
      (die :failed-with-exception (prn-call f-str args)))))
