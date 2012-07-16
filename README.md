kindlemail
==========

Simple utility for sending things to a Kindle.cat README
# kindlemail

Kindlemail is a (very) simple utility for sending files or web-pages to your kindle. 

This project is still in very early Alpha, and will most likely get broken a lot, as I add new features, and attempt to clean things up. I'm still easing into Clojure, and I am still amateur programmer; all constructive criticism welcome. Please email me with any questions, comments, criticisms, or concerns.

## Setup

Kindlemail reads from a .kindlemail config file, saved in the users $HOME directory.

    *NIX:     /home/curtis/.kindlemail
    OSX:      /users/curtis/.kindlemail
    WINDOWS:  C:\Users\curtis\.kindlemail

*note, I don't own Windows or OSX, and I could be completely wrong about the $HOME directories on these OSes.

A template file is provided with kindlemail, and can be copied to $HOME/.kindlemail when run with the -s --setup command.
Or you can just copy it yourself. 

    $ java -jar kindlemail.jar -s KINDLEMAIL_TEMPLATE => copies template to $/.kindlemail

You can also specify a config file to read at run-time with the "-c" "--config" flag

    $ java -ar kindlemail.jar -c CONFIG-FILE TARGET => uses config-file

Simply replace the "CAPS" prompts in the config file to suit your needs.
*note: don't remove the quotes "".

    ~ "TO" -> "curtiswolterding@gmail.com"

Kindlemail uses the postal library written by Andrew Raines - https://github.com/drewr/postal

Currently, kindlemail will only use SMTP and not local mail. (coming soon, hopefully).

The data in the config file is read into the program as a Clojure map. Only change the fields require, or kindlemail won't work.

:Dependencies

    java


## Usage

   Generally, kindlemail is run on a single target file or URL, however you can specify multiple targets and it will mail all of them. Kindlemail will copy the target to a temporary file, email with file attached, and then clean up (delete the temp file). For files or address without a .filetype (.pdf, .txt, etc..), kindlemail will assume you're sending a website, and add a .html to the end of the target. This logic will most likely change soon though. 
    Optionally, you can specify a custom name for the file your sending with the "-f" flag. This works best with a single target to be sent, as it will rename all targets.
    Kindlemail can send to a list of address, useful for sending an article to a class, or reading club. Lists are configured in the lists section of the config file, and are named with a Clojure keyword (:listname). Addresses are added into a vector as strings ["addressone" "addresstwo"]. To mail to a list use the "-l" "--list" flag and the name of the list. 

Use the "-h" "--help" for a list of all options.

As of right now, I haven't rolled kindlemail into any sort of executable form. You'll have to either do that yourself, or run it via the java -jar command. Hopefully I'll get around to changing this soon. 

Basic:
    
    $ java -jar kindlemail.jar TARGET => sends target to whatever you replaced TO with in your config file.

Advanced: 

    $ java -jar kindlemail.jar TARGET1 TARGET2 TARGET3... => mail all targets

    $ java -jar kindlemail.jar -l LIST -f SPECIFIC-FILENAME TARGET => mail target, renamed specific-filename, to everyone in list

## License

Copyright (C) 2012 Curtis Wolterding - curtiswolterding@gmail.com

Distributed under the Eclipse Public License, the same as Clojure.
