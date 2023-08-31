package io.frankmayer.papermcwebapi.utils;

public class HtmlTemplate {
    public static String li(final Iterable<String> values) {
        final StringBuilder sb = new StringBuilder();
        for (final String scope : values) {
            sb.append("<li>").append(scope).append("</li>");
        }
        return sb.toString();
    }

    public static String li(final String... values) {
        final StringBuilder sb = new StringBuilder();
        for (final String scope : values) {
            sb.append("<li>").append(scope).append("</li>");
        }
        return sb.toString();
    }

    private final StringBuilder body = new StringBuilder();

    public HtmlTemplate() {
    }

    public HtmlTemplate append(final String value) {
        this.body.append(value);
        return this;
    }

    public String process(final Object... args) {
        return Str.html(String.format(this.body.toString(), args));
    }

    @Override
    public String toString() {
        return Str.html(this.body.toString());
    }

    public HtmlTemplate p(final Object content) {
        return this.append("<p>").append(content.toString()).append("</p>");
    }

    public HtmlTemplate ul(final Object content) {
        return this.append("<ul>").append(content.toString()).append("</ul>");
    }

    public HtmlTemplate form(final String method,
            final String action,
            final boolean autocomplete,
            final Object content) {
        return this.append(
                String.format(
                        "<form method=\"%s\" action=\"%s\"%s>",
                        method,
                        action,
                        autocomplete ? "" : " autocomplete=\"off\""))
                .append(content.toString())
                .append("</form>");
    }

    public HtmlTemplate input(final String type, final String name, final boolean required, final String value) {
        return this.append(
                String.format(
                        "<input type=\"%s\" name=\"%s\" value=\"%s\"%s/>",
                        type == null ? "text" : type,
                        name == null ? "" : name,
                        value == null ? "" : value,
                        required ? " required" : ""));
    }

    public HtmlTemplate input(final String type, final String name, final boolean required) {
        return this.append(
                String.format(
                        "<input type=\"%s\" name=\"%s\"%s/>",
                        type,
                        name,
                        required ? " required" : ""));
    }
}
