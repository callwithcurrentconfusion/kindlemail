(ns kindlemail.filetype)
;; TODO: Derive filetypes, avoid redundancy
;; TODO: work on matching .com .org .net addresses, maybe use
;; .getFile? seems to get the hosted file referenced by a url. 

;; allowed kindle filetypes
(def ^:private kindle-native-filetypes
  '(".AZW" ".TXT" ".PDF" ".MOBI" ".PRC" ".ZIP"))
(def ^:private kindle-conversion-filetypes
  '(".HTML" ".DOC" ".DOCX" ".JPEG" ".GIF" ".PNG" ".BMP" ".AA" ".AAX" ".MP3" ".KF8"))
;; allowed kindle fire filetypes
(def ^:private fire-native-filetypes
  '(".AZW" ".TXT" ".PDF" ".MOBI" ".PRC" ".ZIP"))
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

;; default "" devicetype (for unconfigured device)
(defmethod ^:private kindle-filetypes :default [_]
  (fn [ext]
    "convert"))

;; check file-type: config, string -> "convert" "" | nil
(defn- check-filetype
  "Check a filetype against the allow filetypes for a device. return the subject of the email."
  [confm ft]
  ((kindle-filetypes confm) (.toUpperCase ft)))

;; TODO: figure out better way to do this? MIME?
;; extract-filetype: string -> string ".ext" | default ("")
(defn extract-filetype
  "Get the filetype from a target-string based on the .ext only. If we fail, use an optional default or an empty string"
  ([target & default]
     (if-let [extracted (->> target
                             (re-matches #".*(\.[\d\w]+)")
                             last)]
       ;; If we matched any filetype, use that.
       extracted
       ;; Else return our default filetype.
       (first default)))
  ([target]
     (extract-filetype target "")))

;; create-subject: map, string -> string
(defn create-subject
  "Generate the email subject bbased on filetype and user config file."
  [confm ft]
  (if (some #(= ft %) (get confm :convert)) ; check config file for override
    "convert"
    ;; else go with whatever our supported filetypes say.
    (check-filetype confm ft)))

(defn get-hosted-file
  "Return the portion of a URL representing the hosted file as a string."
  [target]
  (.getFile (java.net.URL. target)))