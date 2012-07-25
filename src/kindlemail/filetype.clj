(ns kindlemail.filetype)

;; TODO: Zip files?
;; allowed kindle filetypes
(def ^:private kindle-native-filetypes
  '(".AZW" ".TXT" ".PDF" ".MOBI" ".PRC"))
(def ^:private kindle-conversion-filetypes
  '(".HTML" ".DOC" ".DOCX" ".JPEG" ".GIF" ".PNG" ".BMP" ".AA" ".AAX" ".MP3" ".KF8"))
;; allowed kindle fire filetypes
(def ^:private fire-native-filetypes
  '(".AZW" ".TXT" ".PDF" ".MOBI" ".PRC"))
(def ^:private fire-conversion-filetypes
  '(".DOC" ".DOCX" ".MP3" ".AAC" ".MIDI" ".OGG" ".WAV" ".JPEG" ".GIF"
    ".PNG" ".BMP" ".MP4" ".VP8" ".HTML"))

(defmulti ^:private kindle-filetypes
  "Creates a fn for checking the filetype against a device." :device)

(defmethod ^:private kindle-filetypes "fire" [_]
  (fn [ext]
    (cond
     (some #(= ext %) fire-native-filetypes) ""
     (some #(= ext %) fire-conversion-filetypes) "convert"
     :else (throw (Exception. "This filetype is not supported by your device.")))))

(defmethod ^:private kindle-filetypes "kindle" [_]
  (fn [ext]
    (cond
     (some #(= ext %) kindle-native-filetypes) ""
     (some #(= ext %) kindle-conversion-filetypes) "convert"
     :else (throw (Exception. "This filetype is not supported by your device.")))))

;; extract-filetype: string -> string ".ext" | ""
(defn- extract-filetype
  "Get the filetype from a target-string based on the .ext only."
  [target]
  (if-let [extracted (->> target
                          (re-matches #".*(\.[\d\w]+)")
                          last)]
    ;; if we get something uppercase it
    (.toUpperCase extracted)
    ;; else return an empty string
    ""))

;; check file-type: string -> "convert" "" | nil
(defn check-filetype
  "Check a filetype against the allow filetypes for a device. return the subject of the email."
  [confm ft]
  ((kindle-filetypes confm) ft))

;; create-subject: map, string -> string
(defn create-subject
  "Generate the email subject based on filetype and user config file."
  [confm target]
  (let [ft (extract-filetype target)]
    (if (some #(= ft %) (get confm :convert)) ; check config file for override
      "convert"
      (check-filetype confm ft))))