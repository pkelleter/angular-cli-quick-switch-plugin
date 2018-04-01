# angular-cli-quick-switch-plugin

IntelliJ Plugin for switching between Angular CLI component files efficiently (ts -> html -> css -> scss) by using just a shortcut.

For example let's say we have a structure like this

    foo/
        foo.component.html
        foo.md
        foo.component.scss
        foo.component.ts

and we have selected the file **foo.component.ts**. By pressing the assigned shortcut repeatedly, the IDE will cycle
the files (i.e. closing the current tab and replacing it by the next one in the cycle) as follows:

    foo.component.ts -> foo.component.html -> foo.component.scss -> foo.component.ts

Whilst the default shortcut is registered with **Alt + S**, you can of course easily adjust this in the keymap settings by replacing it for **QuickSwitch**.