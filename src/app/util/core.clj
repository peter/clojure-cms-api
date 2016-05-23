(ns app.util.core
  (:require [clojure.string :as str]))

(defn parse-int
      "idempotent string to integer conversion"
      [string-or-int]
  (if (string? string-or-int) (. Integer parseInt string-or-int) string-or-int))

(defn safe-parse-int [string-or-int]
  (try
    (parse-int string-or-int)
    (catch java.lang.NumberFormatException e nil)))

(defn parse-bool [value]
  (not (contains? #{nil false "0" "f" "false"} value)))

(defn load-var [path]
  (let [[m f] (str/split path #"/")
        ns-path (symbol m)]
    (require ns-path)
    (ns-resolve (find-ns ns-path) (symbol f))))

(defn round
  "Round a double to the given precision (number of significant digits)"
  [d & {:keys [digits] :or {digits 0}}]
  (let [factor (Math/pow 10 digits)
        result (/ (Math/round (* d factor)) factor)]
    (if (= digits 0)
      (Math/round result)
      result)))

(defn percent [from to & {:keys [digits] :or {digits 0}}]
  (round (* 100 (/ (- to from) from)) :digits digits))

(defn array [value]
  (if (= clojure.lang.PersistentVector (class value))
    value
    [value]))

(defn filter-property [docs name value]
  (filter #(= (name %) value) docs))

(defn find-property [docs name value]
  (first (filter-property docs name value)))

(defn split-by-comma [value]
  (if (string? value) (str/split value #",") value))

; Map function over values in a map, see: http://stackoverflow.com/questions/1676891/mapping-a-function-on-the-values-of-a-map-in-clojure
(defn map-values [f m]
  (reduce (fn [altered-map [k v]] (assoc altered-map k (f v))) {} m))

; Wrap a function in a nil check, i.e. only execute function if value is not nil
(defn maybe [f]
  (fn [value]
    (if (nil? value)
      value
      (f value))))

; See https://github.com/puppetlabs/clj-kitchensink
(defn deep-merge
  "Deeply merges maps so that nested maps are combined rather than replaced.
  For example:
  (deep-merge {:foo {:bar :baz}} {:foo {:fuzz :buzz}})
  ;;=> {:foo {:bar :baz, :fuzz :buzz}}
  ;; contrast with clojure.core/merge
  (merge {:foo {:bar :baz}} {:foo {:fuzz :buzz}})
  ;;=> {:foo {:fuzz :quzz}} ; note how last value for :foo wins"
  [& vs]
  (if (every? map? vs)
    (apply merge-with deep-merge vs)
    (last vs)))

; See http://dev.clojure.org/jira/browse/CLJ-1468
(defn deep-merge-with
  "Like merge-with, but merges maps recursively, appling the given fn
  only when there's a non-map at a particular level.

  (deepmerge + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
               {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
  -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}"
  [f & maps]
  (apply
    (fn m [& maps]
      (if (every? map? maps)
        (apply merge-with m maps)
        (apply f maps)))
    maps))

(defn partial-right
  "Takes a function f and fewer than the normal arguments to f, and
 returns a fn that takes a variable number of additional args. When
 called, the returned function calls f with additional args + args."
  ([f] f)
  ([f arg1]
   (fn [& args] (apply f (concat args [arg1]))))
  ([f arg1 arg2]
   (fn [& args] (apply f (concat args [arg1 arg2]))))
  ([f arg1 arg2 arg3]
   (fn [& args] (apply f (concat args [arg1 arg2 arg3]))))
  ([f arg1 arg2 arg3 & more]
   (fn [& args] (apply f (concat args (concat [arg1 arg2 arg3] more))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; compact multimethod
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti compact map?)

(defmethod compact true [map]
  (into {} (remove (comp nil? second) map)))

(defmethod compact false [col]
  (remove nil? col))
