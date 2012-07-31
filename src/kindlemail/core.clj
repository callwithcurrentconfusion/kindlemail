(ns kindlemail.core
  (:require [postal.core :only send-message]
            [clojure.tools.cli :only cli])  ;; send-message and command-line tools
  (:use kindlemail.file
        kindlemail.config
        kindlemail.filetype
        clojure.repl) ;; debugging, creating, and reading a .kindlemail config
  (:gen-class :main true)) ;; aot compiling for executable jarfile

;; TODO: TEST
;; TODO: have download file return both file and subject, and
;; mail-file will use both of them
;; TODO: add optional target file for permanant saving.
;; TODO: optional conversion of file (maybe use calibre)
;; TODO  enhance the entire send-message command. allow people to use other email services (local email)
;;       *note, yahoo requires creating an authenticator object to send with the mail.

;; TODO: RSS feeds in config file
;; TODO: Exceptions: un-found config, failed download, failed mail...
;; TODO: lzpack? shell, scripts or something.
;; TODO: Catch if kindlemail is run without a modified config-file
;; TODO: defrecord for parcel with file, name, and other payload options
;;       config file will be reserver for mailing the message and mainly used by mail-file
;; TODO: Application specific passwords for google (for extra security
;; on some accounts)
;; TODO: get-in seems to always eval it's fail argument (at least when
;; it throws or side-effects.) figure out some way to fix this.
;; TODO: config file - add optional text to prepend all sent files
;; with. ("kindlemail-" -> "kindlemail-file.pdf")


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
  [f to-list]
  ;; mail to each address in to-list
  (def subject (create-subject *confm* (extract-filetype (.getName f))))
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



(defn create-send-list
  "Create a to-list based on config file and a list-keyword or nil."
  [confm list-arg]
  (if list-arg
    ;; look in the :lists values for a key named list-arg
    (get-in confm [:lists list-arg] [])
    ;; else send to the :to value in config
    [(:to confm)]))

(defn post-mail
  "Dispatch the mail."
  [arg name f to]
  (-> arg
      (f name)
      ; (try (mail-file to) ...
      (mail-file to)
      delete-file))

;; **** MAIN ****
;; -main: array-seq of args -> boolean 
(defn -main
  "download, mail, and delete file from address."
  [& args]
  (let [[opts a doc]
        (clojure.tools.cli/cli
         args
         ["-h" "--help" "Show this dialogue." :flag true]
         ["-n" "--name" "Specify a new name for the file to be sent.\n You must specify a filetype, i.e. .pdf, .html, etc."]
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
                 ;; dispatch based on local/remote and name
                 (if (local-file? arg)
                   ;; local
                   (if name
                     (post-mail arg
                                (:name opts)
                                local-download
                                (create-send-list *confm* (:list opts)))
                     ;; not renaming, just mail and be done
                     (mail-file (clojure.java.io/file arg)
                                (create-send-list *confm* (:list opts))))
                   ;; remote
                   (post-mail arg
                              (:name opts)
                              remote-download
                              (create-send-list *confm* (:list opts))))))))))



;; check-everything
;; valid file? valid url
;; create parcel
;; subject, file, filetype, to from, etc

;; what needs what

;; name      __, file, ______, _______, name, ext, remote/local

;; download  __, file, ______, _______, name, ___, remote/local 

;; subject   __, ____, config, _______, ____, ext

;; mail:     to, file, config, subject








