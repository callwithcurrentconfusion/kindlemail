(ns kindlemail.filetype)

;; TODO: Zip files?
;; allowed kindle filetypes
(def kindle-native-filetypes
  '(".AZW" ".TXT" ".PDF" ".MOBI" ".PRC"))
(def kindle-conversion-filetypes
  '(".HTML" ".DOC" ".DOCX" ".JPEG" ".GIF" ".PNG" ".BMP" ".AA" ".AAX" ".MP3" ".KF8"))
;; allowed kindle fire filetypes
(def fire-native-filetypes
  '(".AZW" ".TXT" ".PDF" ".MOBI" ".PRC"))
(def fire-conversion-filetypes
  '(".DOC" ".DOCX" ".MP3" ".AAC" ".MIDI" ".OGG" ".WAV" ".JPEG" ".GIF"
    ".PNG" ".BMP" ".MP4" ".VP8" ".HTML"))

(defmulti kindle-filetypes
  "Creates a fn for checking the filetype against a device." :device)

(defmethod kindle-filetypes "fire" [_]
  (fn [ext]
    (cond
     (some #(= ext %) fire-native-filetypes) ""
     (some #(= ext %) fire-conversion-filetypes) "convert"
     :else nil)))

(defmethod kindle-filetypes "kindle" [_]
  (fn [ext]
    (cond
     (some #(= ext %) kindle-native-filetypes) ""
     (some #(= ext %) kindle-conversion-filetypes) "convert"
     :else nil)))

;; extract-filetype: string -> string ".ext" | nil
(defn- extract-filetype
  "Get the filetype from a target-string based on the .ext only."
  [target]
  (->> target
       (re-matches #".*(\.[\d\w]+)")
       last))

;; check file-type: string -> "convert" "" | nil
(defn check-filetype
  "Check a filetype against the allow filetypes for a device. return the subject of the email."
  [confm target]
  (let [ft (.toUpperCase (extract-filetype target))]
    ((kindle-filetypes confm) ft)))