(ns kindlemail.file
  (:require [clojure.java.io :as io]
            [kindlemail.filetype :only extract-filetype]))

;; TODO: use url filename instead of page title if possible.

(def tmpdir (System/getProperty "java.io.tmpdir"))

(defn create-km-filename
  "Create a string representing a kindlemail filename."
  ([name]
     (str tmpdir "/" name))
  ([name filetype]
     (str tmpdir "/" name filetype)))

;; find-page-title: string -> string
(defn find-page-title
  "Match the <title> TITLE </title> of a webpage."
  [page]
  (last
  ;; (?s) will match \newlines, ?i will ignore case
  (re-find #"(?i)<TITLE>[\n]*(.*)[\n]*</TITLE>" page)))

;; copy-remote: URL, file -> file
;; reader = characters
;; stream = bytes
(defn- copy-remote
  "Write a URL into a file"
  [url file]
  (with-open [in  (io/input-stream url)
              out (io/output-stream file)]
    (io/copy in out))
  file)

;; remote-download: string, string | nil -> file
;; NOTE: slow now, because we're copying a url to a file, and then a
;; file to a string. maybe we can regex as bytes on the stream or file?
(defn remote-download
  "download a remote file, renaming it with name, or extracting a name for it."
  [target name]
  (def u (io/as-url target))
  ;; assume filetype checked with name
  (if name
    (copy-remote u (io/file (create-km-filename name)))
    ;; (gotta be a better way to do this)!
    (let [tmpfile (copy-remote u (java.io.File/createTempFile "kindlemail" nil))
          ;; our remote file is now saved temporarly in a tmpfile. We need
          ;; to rename it now, with either it's own title (if it's a
          ;; webpage) or with the filename part of the url
          f       (.getFile u)
          ft      (kindlemail.filetype/extract-filetype f ".html")
          renamed-file (clojure.java.io/file
                        (if (= ".HTML" (.toUpperCase ft))
                          ;; if it's a webpage (.html) get the title
                          (create-km-filename 
                           (find-page-title (slurp tmpfile)) ".html")
                          ;; else name it after it's filename
                          (create-km-filename
                           (last (clojure.string/split f #"/")))))]
      (.renameTo tmpfile renamed-file)
      renamed-file)))

;; local-download: string, string -> file
(defn local-download
  "Copy local file with rename. for non-renames we'll skip this entire process."
  [file-name new-name]
  ;; assume filetype has been checked already
  (let [f (io/file file-name)
        tmpfile (io/file (create-km-filename new-name))]
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

