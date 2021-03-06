Kindlemail
==========

Kindlemail is a quick-n-dirty utility for sending files or web-pages to your kindle. 

This project is still in very early alpha, and will most likely 
get broken a lot as I add new features, and attempt to clean things up. 
I'm still learning Clojure, and I am still amateur programmer; 
all constructive criticism is welcome. 
Please email me with any questions, comments, criticisms, or concerns.

##Building

If you have Leiningen installed, you can build with a simple:

   $ lein uberjar

Not using Leiningen, you can either build your jar with Clojure and Java, or ask me to send you one.
I'll eventually put binaries somewhere public.

##Configuring

Kindlemail reads from a .kindlemail config file, saved in the users $HOME directory.

>  LINUX:    /home/curtis/.kindlemail
>  OSX:      /users/curtis/.kindlemail
>  WINDOWS:  C:\Users\curtis\\.kindlemail

A skeleton file is provided with kindlemail, and can be copied to $HOME/.kindlemail 
by running with the -s --setup command. Or you can just copy it yourself. 

    $ java -jar kindlemail.jar -s KINDLEMAIL_SKEL => copies template to $/.kindlemail

You can also specify a config file to read at run-time with the "-c" "--config" flag

    $ java -ar kindlemail.jar -c CONFIG-FILE TARGET => uses config-file

Simply replace the "CAPS" prompts in the config file to suit your needs.
*note: don't remove the quotes "".

>  ~ "TO" -> "yourname@kindle.com"

Kindlemail uses the postal library written by Andrew Raines - https://github.com/drewr/postal

Currently, kindlemail will only use SMTP and not local mail. (coming soon, hopefully).
Using yahoo mail is throwing exceptions. I'll have to work on this for future releases.
For now, all that's been successfully tested is Gmail.

The data in the config file is read into the program as a map. 
Only change the fields require, or kindlemail won't work.


## Using

Generally, kindlemail is run on a single target file or URL, 
however you can specify multiple targets and it will mail all of them. 
Kindlemail will copy the target to a temporary file, email with file attached, 
and then clean up (delete the temp file). For files or address without a 
.filetype (.pdf, .txt, etc..), kindlemail will assume you're sending a website, 
and add a .html to the end of the target. This logic will most likely change soon though. 
Optionally, you can specify a custom name for the file your sending with the "-f" flag. 
This works best with a single target to be sent, as it will rename all targets.
Kindlemail can send to a list of address, for sending an article to a class, or to a book club. 
Lists are configured in the lists section of the config file, 
and are named with a Clojure keyword (:listname). Addresses are added into a vector as strings 
["addressone" "addresstwo"]. To mail to a list use the "-l" "--list" flag and the name of the list. 

Use the "-h" "--help" for a list of all options.

As of right now, I haven't packaged kindlemail into any sort of executable form. 
You'll have to either do that yourself, or run it via the java -jar command. 
Hopefully I'll get around to changing this soon. 

Basic:
    
    $ java -jar kindlemail.jar TARGET => sends target to whatever you replaced TO with in your config file.

Advanced: 

    $ java -jar kindlemail.jar TARGET1 TARGET2 TARGET3... => mail all targets

    $ java -jar kindlemail.jar -l LIST -f SPECIFIC-FILENAME TARGET => mail target, renamed specific-filename, to everyone in list

## License

Copyright (C) 2012 Curtis Wolterding - curtiswolterding@gmail.com

Distributed under the Eclipse Public License, the same as Clojure.
