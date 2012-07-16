(ns kindlemail.file
  (:require [clojure.java.io :as io]))

;; allowed kindle filetypes
(def kindle-native-filetypes
  '(".AZW" ".TXT" ".PDF" ".MOBI" ".PRC"))
(def kindle-conversion-filetypes
  '(".HTML" ".DOC" ".DOCX" ".JPEG" ".GIF" ".PNG" ".BMP" ".AA" ".AAX" ".MP3" ".KF8"))
;;
;; .zip files are expanded and converted
;; 
;; allowed kindle fire filetypes
(def fire-native-filetypes
  '(".AZW" ".TXT" ".PDF" ".MOBI" ".PRC"))
(def fire-conversion-filetypes
  '(".DOC" ".DOCX" ".MP3" ".AAC" ".MIDI" ".OGG" ".WAV" ".JPEG" ".GIF"
    ".PNG" ".BMP" ".MP4" ".VP8"))

;; check file-type: string -> string or nil
(defn check-file-type
  "Determine the filetype for a given address.
   www.something.com/download/file.pdf -> \".PDF\"
   Return nil if unable to match anything."
  [address]
  (let [ft (->> address
                (re-matches #".*(\.[\d\w]+)")
                last)]
    (when ft ;; if we don't have a nil re-match
      (when (or (some #(= % (clojure.string/upper-case ft)) kindle-native-filetypes)
                (some #(= % (clojure.string/upper-case ft)) kindle-conversion-filetypes))
        ft))))

;; better-download: target (url or file), string | nil -> file
;; TODO: handle FileNotFoundException for non-local file and unreachable url
(defn better-download
  "save to /tmp but don't use tempfile"
  [target name]
  (let [t (io/file target)                           ; local file?
        tmpdir (System/getProperty "java.io.tmpdir") ; /tmp
        filetype (check-file-type target)
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

;; delete file: string -> boolean
(defn delete-file
  "Delete the temporary file"
  [f]
  (.delete f))