(ns kindlemail.config)

;; find-config [] -> file 
;; can find with (System/getProperty "user.home")
(defn find-config
  "Locate the .kindlemail configuration file, whether it's been created yet or not."
  []
  (let [home (System/getProperty "user.home")]
    (clojure.java.io/file (str home "/.kindlemail"))))

;; config->map: string -> map | throw Exception
;; need exception handling here.
;; failed slurp of unfound file
;; java.io.FileNotFoundException
(defn config->map
  "Find a config file, and load it up for use by other functions."
  [filename]
  (try (read-string (slurp filename))
       (catch java.io.FileNotFoundException e
         (println (str "Couldn't read from " (.toString filename) ".\nMake sure it exists and is readable."))
         nil)))

;; prompt-for: "prompt" seq-of-matches -> boolean
;; *note: readline doesn't play well with slime's repl*
(defn prompt-for
  "Prompt a user with p and return true if their input matchs m"
  [p m]
  (println p)
  (let [response (.toUpperCase ;"yes"
                               (read-line)
                               )]
    (some #(= response %) m)))

;; generate-config: file file -> nil
(defn generate-config
  "copy template to f"
  [template f]
  ;; for now do something as simple 
  (println (str "YES: Copying config file " template " to " f "."))
  (println "Edit this file for future use, or specify another config to use at runtime with the \"-c\" flag.")
  (clojure.java.io/copy template f))

;; FIXME: doesn't seem to be evaluating the and expression
;; kindlemail-setup: string -> nil
(defn kindlemail-setup
  "copy the kinlemail skeleton file to home/.kindlemail"
  [template]
  (let [conf-file (find-config)]
    (when (.exists conf-file)
      (println (str "Config file already exists at: " conf-file)))
    (when (prompt-for (str "Copy " template " to " (.toString conf-file)
                           "overwriting any pre-existing file?\n(yes/no)") ["YES" "Y"])
      (generate-config (clojure.java.io/file template) conf-file))))



