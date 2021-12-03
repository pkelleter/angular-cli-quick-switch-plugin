# React Quick Switch Plugin

Fork of [Angular CLI QuickSwitch](https://github.com/pkelleter/angular-cli-quick-switch-plugin)

IntelliJ Plugin for switching amongst React component files efficiently (e.g. tsx -> scss) by just using one shortcut (**Alt+S**).

For example let's say we have a structure like this

    Foo/
        Bar.info
        FooComponent.tsx
        FooComponent.scss
        Foo.md

By pressing the assigned shortcut repeatedly, the IDE will cycle amongst the files as follows:

    FooComponent.tsx -> FooComponent.scss -> FooComponent.tsx -> ...


Whilst cycling, unknown filenames (like bar.info) and extensions (like foo.md) will be skipped on purpose.

Explicitly whitelisted extensions are:
- tsx, ts, jsx, js
- html, php, haml, jade, pug, slim
- css, sass, scss, less, styl

Note:
For switching between your code and test/spec files, you can simply use the built-in default shortcut from IntelliJ (**Ctrl+Shift+T**)



Further information:

If the default shortcut **Alt + S** does not fit your needs, you can of course easily adjust it in the keymap settings by replacing it for **QuickSwitch**.

Due to the nature of this plugin, it will of course also **work with other projects**,
not even React projects - as long as your project structure follows similar patterns.