package patrick.kelleter.angularcliquickswitch;

enum QuickSwitchFileType {
    TYPESCRIPT(Category.CONTROLLER, "ts", true),
    JAVASCRIPT(Category.CONTROLLER, "js", true),

    HTML(Category.TEMPLATE, "html", true),
    PHP(Category.TEMPLATE, "php", true),
    HAML(Category.TEMPLATE, "haml", true),
    JADE(Category.TEMPLATE, "jade", true),
    PUG(Category.TEMPLATE, "pug", true),
    SLIM(Category.TEMPLATE, "slim", true),

    CSS(Category.STYLE, "css", true),
    SASS(Category.STYLE, "sass", true),
    SCSS(Category.STYLE, "scss", true),
    LESS(Category.STYLE, "less", true),
    STYLUS(Category.STYLE, "styl", true),

    TYPESCRIPT_SPEC(Category.TESTS, "spec.ts", false),
    JAVASCRIPT_SPEC(Category.TESTS, "spec.js", false);

    enum Category {
        CONTROLLER,
        TEMPLATE,
        STYLE,
        TESTS
    }

    private final Category category;
    private final String suffix;
    private final boolean enabledByDefault;

    QuickSwitchFileType(Category category, String suffix, boolean enabledByDefault) {
        this.category = category;
        this.suffix = suffix;
        this.enabledByDefault = enabledByDefault;
    }

    Category category() {
        return category;
    }

    String suffix() {
        return suffix;
    }

    boolean enabledByDefault() {
        return enabledByDefault;
    }
}
