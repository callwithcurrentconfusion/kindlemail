(ns kindlemail.core
  (:require [postal.core :only send-message]
            [clojure.tools.cli :only cli])  ;; send-message and command-line tools
  (:use kindlemail.file
        kindlemail.config
        kindlemail.filetype) ;; debugging, creating, and reading a .kindlemail config
  (:gen-class :main true)) ;; aot compiling for executable jarfile

;; TODO: parse the html to find the <title> and use that for a file-name if desired.
;; TODO: add optional target file for permanant saving.
;; TODO: optional conversion of file (maybe use calibre)
;; TODO: check filetype is good for kindle, and send that filetype. .pdf .html, etc
;;       good use of this would be a multimethod for the differen kindle models
;; TODO  enhance the entire send-message command. allow people to use other email services (local email)
;;       *note, yahoo requires creating an authenticator object to send with the mail.

;; TODO: SKIP COPYING LOCAL FILES TO /TMP, JUST MAIL THEM WITHOUT DELETING.

;; TODO: RSS feeds in config file
;; TODO: Exceptions: un-found config, failed download, failed mail...
;; TODO: lzpack? shell, scripts or something.
;; TODO: Catch if kindlemail is run without a modified config-file
;; TODO: optional Convert! parameter in config file for .pdf, .html etc
;; TODO: defrecord for parcel with file, name, and other payload options
;;       config file will be reserver for mailing the message and mainly used by mail-file
;; TODO: Application specific passwords for google (for extra security on some accounts)

;; **** GLOBALs ****
;; *****************
;; user configuration will be bound to (config-map (find-config)) in -main threa'd
(declare ^:dynamic *confm*)

;; mail: file, map, to-list -> file
;; mail exceptions to catch: FIXME
;; TODO: check status of mail. catch exceptions
;; NOTE: Try-catch doesn't seem to catch javax.mail.MessagingException
(defn mail-file
  "Mail the file.
   If a list-key is provided, create a list and dispatch mail to all addr on list."
  [f to-list subject]
  ;; mail to each address in to-list
  (doseq [addr to-list]
    (prn (postal.core/send-message ^{:host (:host *confm*)
                                         :user (:user *confm*)
                                         :pass (:pass *confm*)
                                         :ssl :yes}
                                       {:from (:from *confm*)
                                        :to addr
                                        ;; :subject "convert"
                                        :subject subject
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
       (get-in *confm* [:lists (keyword list-arg)] (throw (Exception. (str "List " list-arg
                                                                           " is not listed in your config."))))
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
         ["-f" "--file" "Specify a new name for the file to be sent.\n You must specify a filetype, i.e. .pdf, .html, etc."]
         ["-l" "--list" "Mail to a list declared in .kindlemail."]
         ["-c" "--config" "Use a specific config file." :default (find-config)]
         ["-s" "--setup" "Copy a config to $HOME/.kindlemail."]
;         ["-v" "--verbose" "Verbose mode." :flag true]
         )
        filename (:file opts)]
    (cond
     (:help opts) (println doc)
     (:setup opts) (kindlemail-setup (:setup opts))
     ;; we have side effects so we need to force evaulation on the entire sequence of arguments
     :else (binding [*confm* (config->map (:config opts))]
             (when *confm* ; we found and read our config file
               (doseq [arg a]
                 ))))))

;; check-everything
;; valid file? valid url
;; create parcel
;; subject, file, filetype, to from, etc

;; what needs what

;; name      __, file, ______, _______, ____, ext, remote/local

;; download  __, file, ______, _______, name, ___, remote/local ->
;; package-details (file, subj)

;; subject   __, ____, config, _______, ____, ext

;; mail:     to, file, config, subject








