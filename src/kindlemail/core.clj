(ns kindlemail.core
  (:require [postal.core :only send-message]
            [clojure.tools.cli :only cli])  ;; send-message and command-line tools
  (:use kindlemail.file
        kindlemail.config
        kindlemail.filetype
        clojure.repl) ;; debugging, creating, and reading a .kindlemail config
  (:gen-class :main true)) ;; aot compiling for executable jarfile

;; BUGS:
;; http://www.pawfal.org/dave/blog/2012/08/algorithmic-fungi-patterns/?utm_source=dlvr.it&utm_medium=twitter
;; breaks it. the File is "/dave/blog/2012/08/algorithmic-fungi-patterns/?utm_source=dlvr.it&utm_medium=twitter"

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
         ["-n" "--name" "Specify a new name for the file to be sent. You must specify a filetype, i.e. .pdf, .html, etc."]
         ["-l" "--list" "Mail to a list declared in .kindlemail."]
         ["-c" "--config" "Use a specific config file." :default (find-config)]
         ["-s" "--setup" "Copy a file to $HOME/.kindlemail. Use initially on the kindlemail_skel file."]
;         ["-v" "--verbose" "Verbose mode." :flag true]
         )
        name (:name opts)]
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
                     (do
                       (println "Sending local file with file rename.")
                       (post-mail arg
                                  (:name opts)
                                  local-download
                                  (create-send-list *confm* (:list opts))))
                     ;; not renaming, just mail and be done
                     (do
                       (println "Sending local file without renaming.")
                       (mail-file (clojure.java.io/file arg)
                                     (create-send-list *confm* (:list opts)))))
                   ;; remote
                   (do
                     (println "Sending remote file.")
                     (post-mail arg
                                (:name opts)
                                remote-download
                                (create-send-list *confm* (:list opts)))))))))))



;; check-everything
;; valid file? valid url
;; create parcel
;; subject, file, filetype, to from, etc

;; what needs what

;; name      __, file, ______, _______, name, ext, remote/local

;; download  __, file, ______, _______, name, ___, remote/local 

;; subject   __, ____, config, _______, ____, ext

;; mail:     to, file, config, subject








