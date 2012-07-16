(ns kindlemail.config)

;; prompt-for: "prompt" seq-of-matches -> boolean
;; *note: readline doesn't play well with slime's repl*
(defn prompt-for
  "Prompt a user with p and return true if their input matchs m"
  [p m]
  (print p)
  (let [response (read-line)]
    (some #(= response %) m)))

;; generate-config: file -> file
(defn generate-config
  "copy template to f"
  [template f]
  ;; for now do something as simple 
  (println (str "Copying config file " template "to " f "."))
  (println "Edit this file for future use, or specify another config to use at runtime with the \"-c\" flag.")
  (clojure.java.io/copy template f))

;; find-config [] -> file | nil
(defn find-config
  "Locate the .kindlemail configuration file.
   If not found, return nil"
  []
  (let [home (.get (System/getenv) "HOME")]
    (clojure.java.io/file (str home "/.kindlemail"))))

;; kindlemail-setup: file -> nil
(defn kindlemail-setup
  "copy the kinlemail skeleton file to home/.kindlemail"
  [template]
  (let [conf-file (find-config)]
    (when (.exists conf-file)
      (println (str "Config file already exists at: " conf-file)))
    (and (prompt-for
          (str "Copy " template " to " (.toString conf-file) "
                overwriting any pre-existing file? (yes/no)> ") "yes")
         (generate-config template conf-file))))


;; need exception handling here.
;; failed slurp of unfound file
;; java.io.FileNotFoundException
(defn config->map
  "Find a config file, and load it up for use by other functions."
  [f]
  (try (read-string (slurp f))
       (catch java.io.FileNotFoundException e
         (println (str "Couldn't read from " (.toString f) ".\nMake sure it's readable and exists!")))))
