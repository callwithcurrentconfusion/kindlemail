(ns kindlemail.filetype)
;; TODO: Derive filetypes, avoid redundancy
;; TODO: work on matching .com .org .net addresses, maybe use
;; .getFile? seems to get the hosted file referenced by a url. 

;; allowed kindle filetypes
(def kindle-native-filetypes
  '(".AZW" ".TXT" ".PDF" ".MOBI" ".PRC" ".ZIP"))
(def kindle-conversion-filetypes
  '(".HTML" ".DOC" ".DOCX" ".JPEG" ".GIF" ".PNG" ".BMP" ".AA" ".AAX" ".MP3" ".KF8"))
;; allowed kindle fire filetypes
(def fire-native-filetypes
  '(".AZW" ".TXT" ".PDF" ".MOBI" ".PRC" ".ZIP"))
(def fire-conversion-filetypes
  '(".DOC" ".DOCX" ".MP3" ".AAC" ".MIDI" ".OGG" ".WAV" ".JPEG" ".GIF"
    ".PNG" ".BMP" ".MP4" ".VP8" ".HTML"))

(defmulti kindle-filetypes
  "Creates a fn for checking the filetype against a device." :device)

(defmethod kindle-filetypes "fire" [_]
  (fn [ext]
    (println (str "Checking " ext " in fire allowed filetypes..." ))
    (cond
     (some #(= ext %) fire-native-filetypes) ""
     (some #(= ext %) fire-conversion-filetypes) "convert"
     :else (throw (Exception. "This filetype is not supported by your device.")))))

(defmethod kindle-filetypes "kindle" [_]
  (fn [ext]
    (println (str "Checking " ext " in kindle allowed filetypes..." ))
    (cond
     (some #(= ext %) kindle-native-filetypes) ""
     (some #(= ext %) kindle-conversion-filetypes) "convert"
     :else (throw (Exception. "This filetype is not supported by your device.")))))

;; does default match anything not specified?
;; (defmethod kindle-filetypes "" [_]
;;   (fn [ext]
;;     "convert"))

;; default "" devicetype (for unconfigured device)
(defmethod kindle-filetypes :default [_]
  (fn [ext]
    (println "No device specified in config; will attempt to convert attached file.")
    "convert"))

;; check file-type: config, string -> "convert" "" | nil
(defn check-filetype
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

