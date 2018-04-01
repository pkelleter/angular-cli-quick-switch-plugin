# angular-cli-quick-switch-plugin

IntelliJ Plugin for switching between Angular CLI component files efficiently by using a shortcut (ts/html/css/scss)
By pressing the assigned shortcut, the plugin will close the current file and open the next file in the cycle instead.

For example let's say we have a structure like this

foo/
    foo.component.html
    foo.md
    foo.component.scss
    foo.component.ts

and we have selected the file "foo.component.ts" - by pressing the assigned shortcut repeatedly, the IDE will cycle
the files (i.e. closing the current one and replacing it by the next one in the cycle) as follows:

"foo.component.ts" -> "foo.component.html" -> "foo.component.scss" -> "foo.component.ts"

The default Shortcut is Alt+S but, of course, can easily be adjusted in the Keymap by replacing the shortcut for "QuickSwitch"