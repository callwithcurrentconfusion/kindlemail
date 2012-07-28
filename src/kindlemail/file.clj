(ns kindlemail.file
  (:require [clojure.java.io :as io]
            [kindlemail.filetype :only extract-filetype]))

;; TODO: fix title parser, maybe use an html parser instead of sketchy
;; regex

(def tmpdir (System/getProperty "java.io.tmpdir"))

;; find-page-title: string -> string
;; FIXME make private
(defn find-page-title
  "Match the <title> TITLE </title> of a webpage."
  [page]
  (last
  ;; (?s) will match \newlines, ?i will ignore case
  (re-find #"(?i)<TITLE>[\n]*(.*)[\n]*</TITLE>" page)))

(defn- copy-remote
  "Write a URL into a file"
  [url file]
  (with-open [rdr  (io/reader url)
              wrtr (io/writer file)]
    (io/copy rdr wrtr)))

(defn remote-download
  "download a remote file, renaming it with name, or extracting a name for it."
  [target name]
  ;; assume filetype checked with name
  (if name
    (let [url  (io/as-url target)
          file (io/file (str tmpdir "/" name))]
      (copy-remote url file)
      file)
    ;; we dont have a name yet, attempt to create it ourselves
    ;; to do this we need to read the page in as a string and regex it
    ;; (gotta be a better way to do this)!
    (let [target (java.net.URL. target)
          page (slurp target) 
          name (if-let [title (find-page-title page)]
                 title
                 "no-name")
          filetype (kindlemail.filetype/extract-filetype (.getFile target) ".html")
          tmpfile (io/file (str tmpdir "/" name filetype))]
      (spit tmpfile page)
      tmpfile)))

;; local-download: string, string -> file
(defn local-download
  "Copy local file with rename. for non-renames we'll skip this entire process."
  [file-name new-name]
  ;; assume filetype has been checked already
  (let [f (io/file file-name)
        tmpfile (io/file (str tmpdir "/" new-name))]
    (io/copy f tmpfile)
    tmpfile))


;; delete file: string -> boolean
(defn delete-file
  "Delete the temporary file"
  [f]
  (.delete f))

(defn local-file?
  "Determine if a string specifies a file on the local system."
  [target]
  (.exists (clojure.java.io/file target)))

;; coerce-by-location: string -> URL | File | nil
;; (defn coerce-by-location
;;   "Turn a target into either a URL or FILE. If neither is possible throw an exception."
;;   [target]
;;   (try
;;     ;; URL
;;     (clojure.java.io/as-url target)
;;     (catch java.net.MalformedURLException e
;;       (let [f (clojure.java.io/file target)]
;;         (if (.exists f)
;;           f
;;           (throw (Exception. "Bad target - not found locally or malformed URL!")))))))


;; better-download: target (url or file), string | nil -> file
;; TODO: handle FileNotFoundException for non-local file and unreachable url
;; (defn better-download
;;   "save to /tmp but don't use tempfile"
;;   [target name ft]
;;   (let [t (io/file target)                           ; local file?
;;         tmpdir (System/getProperty "java.io.tmpdir") ; /tmp
;;         filetype (kindlemail.filetype/check-filetype target)
;;         tmpfile (if (.exists t)
;;                   (io/file (str tmpdir "/"
;;                                 (if name
;;                                   (str name filetype)
;;                                   (.getName t))))
;;                   (io/file (str tmpdir "/" "kindlemail-"
;;                                 (if name
;;                                   name
;;                                   "noname")
;;                                 (if filetype
;;                                   filetype
;;                                   "-noext.html"))))]
;;     (if (.exists t)
;;       ;; local
;;       (io/copy t tmpfile)
;;       ;; remote-file
;;       (with-open [rdr (io/reader target)
;;                   wrtr (io/writer tmpfile)]
;;         (io/copy rdr wrtr)))
;;     tmpfile))

;; TODO: unit-testing: filetypes make a dir of test files, mailing create a decorator and compare it
;; TODO: create a packaging-slip record to pass as an argument with :targetname, :rename, :convert, etc
;;       and functions can pass this along instead of a bunch of other crap.

