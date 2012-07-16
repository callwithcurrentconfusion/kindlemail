(ns kindlemail.core
  (:require [postal.core :only send-message]
            [clojure.tools.cli :only cli])  ;; send-message and command-line tools
  (:use clojure.repl
        kindlemail.file
        kindlemail.config) ;; debugging, creating, and reading a .kindlemail config
  (:gen-class :main true)) ;; aot compiling, public static void main(String[] args) BS.

;; TODO: parse the html to find the <title> and use that for a file-name if desired.
;; TODO: add optional target file for permanant saving.
;; TODO: optional conversion of file (maybe use calibre)
;; TODO: check filetype is good for kindle, and send that filetype. .pdf .html, etc
;;       good use of this would be a multimethod for the differen kindle models
;; TODO  enhance the entire send-message command. allow people to use other email services (local email)
;; TODO: skip copying local files to /tmp, just mail them without deleting.
;; TODO: RSS feeds in config file
;; TODO: Exceptions: un-found config, failed download, failed mail...
;; TODO: lzpack? shell, scripts or something. 

;; **** GLOBALs ****
;; *****************
;; user configuration will be bound to (config-map (find-config)) in -main thread
(declare ^:dynamic *confm*)

;; mail: file, map, to-list -> file
;; mail exceptions to catch: FIXME
;; TODO: check status of mail. catch exceptions
(defn mail-file
  "Mail the file.
   If a list-key is provided, create a list and dispatch mail to all addr on list."
  [f to-list]
  ;; mail to each item in to-list
  (doseq [addr to-list]
    (prn (postal.core/send-message ^{:host (:host *confm*)
                                         :user (:user *confm*)
                                         :pass (:pass *confm*)
                                         :ssl :yes}
                                       {:from (:from *confm*)
                                        :to addr
                                        :subject "convert"
                                        :body [{:type :attachment
                                                :content f
                                                }]})))
  f)

;; create-send-list: list-name -> array-of-strings
(defn create-send-list
  "Create a to-list based on config file and a list-keyword or nil."
  ([list-arg]
     (if list-arg
       ;; look in the :lists values for a key named list-arg
       (get-in *confm* [:lists (keyword list-arg)] [])
       ;; else send to the :to value in config
       [(:to *confm*)])))

;; **** MAIN ****
;; -main: array-seq of args -> boolean
(defn -main
  "download, mail, and delete file from address."
  [& args]
  (let [[opts a doc]
        (clojure.tools.cli/cli
         args
         ["-h" "--help" "Show this dialogue." :flag true]
         ["-f" "--file" "Specify the name of the file to send."]
         ["-l" "--list" "Mail to a named list - declared in .kindlemail."]
         ["-c" "--config" "Use a specific config file." :default (find-config)]
         ["-s" "--setup" "Copy a config to $HOME/.kindlemail."]
;         ["-v" "--verbose" "Verbose mode." :flag true]
         )
        filename (:file opts)]
    (binding [*confm* (config->map (:config opts))] ; defaults to (find-config)
      (def to-list (create-send-list (:list opts))) ; create a to-list
      (cond
       (:help opts) (println doc)
       (:setup opts) (kindlemail-setup (:setup opts))
       ;; we have side effects so we need to force evaulation on the entire sequence of arguments
       :else  (doseq [arg a]
                (-> arg
                    (better-download filename)
                    (mail-file to-list)
                    delete-file))))))




