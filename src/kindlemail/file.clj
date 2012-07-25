(ns kindlemail.file
  (:require [clojure.java.io :as io]
            [kindlemail.filetype :only check-filetype]))

;; better-download: target (url or file), string | nil -> file
;; TODO: handle FileNotFoundException for non-local file and unreachable url
(defn better-download
  "save to /tmp but don't use tempfile"
  [target name]
  (let [t (io/file target)                           ; local file?
        tmpdir (System/getProperty "java.io.tmpdir") ; /tmp
        filetype (kindlemail.filetype/check-filetype target)
        tmpfile (if (.exists t)
                  (io/file (str tmpdir "/"
                                (if name
                                  (str name filetype)
                                  (.getName t))))
                  (io/file (str tmpdir "/" "kindlemail-"
                                (if name
                                  name
                                  "noname")
                                (if filetype
                                  filetype
                                  "-noext.html"))))]
    (if (.exists t)
      ;; local
      (io/copy t tmpfile)
      ;; remote-file
      (with-open [rdr (io/reader target)
                  wrtr (io/writer tmpfile)]
        (io/copy rdr wrtr)))
    tmpfile))

(def tmpdir (System/getProperty "java.io.tmpdir"))

(defn remote-download
  "Download a remote file, optionally renaming it."
  [target-url name]
  )

(defn local-download
  [target-file name])

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
(defn coerce-by-location
  "Turn a target into either a URL or FILE. If neither is possible throw an exception."
  [target]
  (try
    ;; URL
    (clojure.java.io/as-url target)
    (catch java.net.MalformedURLException e
      (let [f (clojure.java.io/file target)]
        (if (.exists f)
          f
          (throw (Exception. "Bad target - not found locally or malformed URL!")))))))

;; TODO: unit-testing: filetypes make a dir of test files, mailing create a decorator and compare it
;; TODO: create a packaging-slip record to pass as an argument with :targetname, :rename, :convert, etc
;;       and functions can pass this along instead of a bunch of other crap.

